package com.rlj.order.service.impl;

import com.rlj.enums.OrderStatusEnum;
import com.rlj.enums.YesOrNo;
import com.rlj.item.pojo.Items;
import com.rlj.item.pojo.ItemsSpec;
import com.rlj.item.service.ItemService;
import com.rlj.order.mapper.OrderItemsMapper;
import com.rlj.order.mapper.OrderStatusMapper;
import com.rlj.order.mapper.OrdersMapper;
import com.rlj.order.pojo.OrderItems;
import com.rlj.order.pojo.OrderStatus;
import com.rlj.order.pojo.Orders;
import com.rlj.order.pojo.bo.PlaceOrderBO;
import com.rlj.pojo.ShopcartBO;
import com.rlj.order.pojo.bo.SubmitOrderBO;
import com.rlj.order.pojo.vo.MerchantOrdersVO;
import com.rlj.order.pojo.vo.OrderVO;
import com.rlj.order.service.OrderService;
import com.rlj.user.pojo.UserAddress;
import com.rlj.user.service.AddressService;
import com.rlj.utils.DateUtil;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private Sid sid;
    @Autowired
    private AddressService addressService;
    @Autowired
    private ItemService itemService;


    @Autowired
    private OrderStatusMapper orderStatusMapper;
    @Autowired
    private OrderItemsMapper orderItemsMapper;
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public OrderVO createOrder(PlaceOrderBO placeOrderBO) {
        SubmitOrderBO submitOrderBO = placeOrderBO.getOrder();
        List<ShopcartBO> shopcartList = placeOrderBO.getItems();
        String userId = submitOrderBO.getUserId();
        String addressId = submitOrderBO.getAddressId();
        String itemSpecIds = submitOrderBO.getItemSpecIds();
        Integer payMethod = submitOrderBO.getPayMethod();
        String leftMsg = submitOrderBO.getLeftMsg();
        Integer postAmount = 0;//包邮费用我们设置为0
        String orderId = sid.nextShort();

        UserAddress userAddress = addressService.queryUserAddress(userId,addressId);
        //1、将上面的信息保存到订单表中
        Orders newOrder = new Orders();
        newOrder.setId(orderId);
        newOrder.setUserId(userId);
        newOrder.setReceiverName(userAddress.getReceiver());
        newOrder.setReceiverMobile(userAddress.getMobile());
        newOrder.setReceiverAddress(userAddress.getProvince()+" "+userAddress.getCity()
        +" "+userAddress.getDistrict()+" "+userAddress.getDetail());
        newOrder.setPostAmount(postAmount);
        newOrder.setPayMethod(payMethod);
        newOrder.setLeftMsg(leftMsg);
        newOrder.setIsComment(YesOrNo.NO.type);//最开始订单肯定没有评价过
        newOrder.setIsDelete(YesOrNo.NO.type);//是否真正删除，肯定是NO，我们进行逻辑删除
        newOrder.setCreatedTime(new Date());
        newOrder.setUpdatedTime(new Date());
        //2、通过userId、itemSpecIds获取商品规格表的参数，并保存到订单表中
        //2.1、我们先要获取前端发过来的下单商品的所有规格ID(它们是以逗号连接的，我们在这里分割一下)
        String itemSpecIdArr[] = itemSpecIds.split(",");
        Integer totalAmount = 0;//商品原价累计
        Integer realPayAmount = 0;//折扣后的实际支付价格累计
        //来一个集合存储结算的商品信息，然后将这个集合中的数据从购物车Redis中删除
        List<ShopcartBO> toBeRemovedShopcartList = new ArrayList<>();
        //2.2、然后根据得到的规格ID查询到对应的价格，然后进行累加
        //我们还需要商品购物车中的数量，来做乘法，但是这是从Redis中获取的，我们这里暂且设置为1
        //int buyCounts = 1;
        for (String itemSpecId:itemSpecIdArr){
            ShopcartBO cartItem = getBuyCountsFromShopcart(shopcartList, itemSpecId);
            int buyCounts = cartItem.getBuyCounts();
            toBeRemovedShopcartList.add(cartItem);
            ItemsSpec itemsSpec = itemService.queryItemsSpecById(itemSpecId);
            totalAmount += itemsSpec.getPriceNormal() * buyCounts;
            realPayAmount += itemsSpec.getPriceDiscount() * buyCounts;
            //2.2.1、根据商品ID。获取商品信息以及图片
            String itemId = itemsSpec.getItemId();
            Items item = itemService.queryItemById(itemId);
            String imgUrl = itemService.queryItemMainImgById(itemId);
            //2.2.2、***在这里就将信息保存到订单-商品关联表中
            String orderItemId = sid.nextShort();
            OrderItems subOrderItem = new OrderItems();
            subOrderItem.setId(orderItemId);
            subOrderItem.setOrderId(orderId);
            subOrderItem.setItemId(itemId);
            subOrderItem.setItemName(item.getItemName());
            subOrderItem.setBuyCounts(buyCounts);
            subOrderItem.setItemSpecId(itemSpecId);
            subOrderItem.setItemSpecName(itemsSpec.getName());
            subOrderItem.setItemImg(imgUrl);
            subOrderItem.setPrice(itemsSpec.getPriceDiscount());//获取最终的支付价格
            orderItemsMapper.insert(subOrderItem);
            //2.2.3、在用户提交订单之后，规格表中需要扣除库存
            itemService.decreaseItemSpecStock(itemSpecId,buyCounts);
        }
        newOrder.setTotalAmount(totalAmount);
        newOrder.setRealPayAmount(realPayAmount);
        ordersMapper.insert(newOrder);
        //3、***保存订单状态表
        OrderStatus waitPayOrderStatus = new OrderStatus();
        waitPayOrderStatus.setOrderId(orderId);
        waitPayOrderStatus.setOrderStatus(OrderStatusEnum.WAIT_PAY.type);
        waitPayOrderStatus.setCreatedTime(new Date());
        orderStatusMapper.insert(waitPayOrderStatus);
        //4、***构建商户订单，用于传给支付中心
        MerchantOrdersVO merchantOrdersVO = new MerchantOrdersVO();
        merchantOrdersVO.setMerchantOrderId(orderId);
        merchantOrdersVO.setMerchantUserId(userId);
        //加上运费，不过这里运费我们设置为0
        //这里传给支付中心的我们就不传真实的价格了，传1分钱
        //merchantOrdersVO.setAmount(1);
        merchantOrdersVO.setAmount(realPayAmount+postAmount);

        merchantOrdersVO.setPayMethod(payMethod);
        //5、***构建自定义订单VO
        OrderVO orderVO = new OrderVO();
        orderVO.setOrderId(orderId);
        orderVO.setMerchantOrdersVO(merchantOrdersVO);
        orderVO.setToBeRemovedShopcartList(toBeRemovedShopcartList);
        return orderVO;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateOrderStatus(String orderId, Integer orderStatus) {
        OrderStatus paidStatus = new OrderStatus();
        paidStatus.setOrderId(orderId);
        paidStatus.setOrderStatus(orderStatus);
        paidStatus.setPayTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(paidStatus);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public OrderStatus queryOrderStatusInfo(String orderId) {
        return orderStatusMapper.selectByPrimaryKey(orderId);
    }

    // 通过延迟消息关闭超时订单，原来通过请求触发关闭所有超时订单的方法弃置
//    @Transactional(propagation = Propagation.REQUIRED)
//    @Override
//    public void closeOrder() {
//        //查询所有未付款订单，判断时间是否超时(1天)。超时则关闭交易
//        //说明一下：我们所有的查询条件的参数都是一个对象，将我们要查询的条件封装到这个对象中进
//        //行查询，这里查询未付款的，所以我们将未付款条件给这个对象，用这个对象作为参数进行查询
//        OrderStatus queryOrder = new OrderStatus();
//        queryOrder.setOrderStatus(OrderStatusEnum.WAIT_PAY.type);
//        List<OrderStatus> list = orderStatusMapper.select(queryOrder);
//        for (OrderStatus os : list){
//            //获得订单创建时间
//            Date createTime = os.getCreatedTime();
//            //和当前时间对比，daysBetween得到的结果是以天为单位
//            int days = DateUtil.daysBetween(createTime, new Date());
//            if (days > 1){
//                //超过一天，调用下面关闭订单的方法
//                doCloseOrder(os.getOrderId());
//            }
//        }
//    }

    @Transactional(propagation = Propagation.REQUIRED)
    void doCloseOrder(String orderId){
        OrderStatus close = new OrderStatus();
        close.setOrderId(orderId);
        close.setOrderStatus(OrderStatusEnum.CLOSE.type);
        close.setCloseTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(close);
    }

    //创建订单方法中，会遍历itemSpecIdArr中每一个specId，此时我们根据该specId拿到Redis中与之对应的购物车中的信息，从而得到buyCounts
    private ShopcartBO getBuyCountsFromShopcart(List<ShopcartBO> shopcartList,String specId){
        for (ShopcartBO cart:shopcartList) {
            if (cart.getSpecId().equals(specId)){
                return cart;
            }
        }
        return null;
    }

}
