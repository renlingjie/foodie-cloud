package com.rlj.order.service.impl.center;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rlj.enums.OrderStatusEnum;
import com.rlj.enums.YesOrNo;
import com.rlj.order.mapper.OrderStatusMapper;
import com.rlj.order.mapper.OrdersMapper;
import com.rlj.order.mapper.OrdersMapperCustom;
import com.rlj.order.pojo.OrderStatus;
import com.rlj.order.pojo.Orders;
import com.rlj.order.pojo.vo.MyOrdersVO;
import com.rlj.order.pojo.vo.OrderStatusCountsVO;
import com.rlj.order.service.center.MyOrdersService;

import com.rlj.pojo.IMOOCJSONResult;
import com.rlj.pojo.PagedGridResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MyOrdersServiceImpl implements MyOrdersService {
    @Autowired
    public OrdersMapperCustom ordersMapperCustom;
    @Autowired
    public OrderStatusMapper orderStatusMapper;
    @Autowired
    public OrdersMapper ordersMapper;
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedGridResult queryMyOrders(String userId, Integer orderStatus, Integer page, Integer pageSize) {
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        if (orderStatus != null){
            map.put("orderStatus",orderStatus);
        }
        PageHelper.startPage(page,pageSize);
        List<MyOrdersVO> list = ordersMapperCustom.queryMyOrders(map);
        return setterPagedGird(list,page);
    }

    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public void updateDeliverOrderStatus(String orderId) {

        OrderStatus updateOrder = new OrderStatus();
        updateOrder.setOrderStatus(OrderStatusEnum.WAIT_RECEIVE.type);
        updateOrder.setDeliverTime(new Date());

        Example example = new Example(OrderStatus.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderId", orderId);
        criteria.andEqualTo("orderStatus", OrderStatusEnum.WAIT_DELIVER.type);
        orderStatusMapper.updateByExampleSelective(updateOrder, example);
    }

    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public boolean updateReceiveOrderStatus(String orderId) {
        OrderStatus updateOrder = new OrderStatus();
        updateOrder.setOrderStatus(OrderStatusEnum.SUCCESS.type);
        updateOrder.setSuccessTime(new Date());

        Example example = new Example(OrderStatus.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderId", orderId);
        criteria.andEqualTo("orderStatus", OrderStatusEnum.WAIT_RECEIVE.type);
        int result = orderStatusMapper.updateByExampleSelective(updateOrder, example);
        return result == 1 ? true : false;
    }
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public boolean deleteOrder(String userId, String orderId) {

        Orders updateOrder = new Orders();
        updateOrder.setIsDelete(YesOrNo.YES.type);//?????????????????????
        updateOrder.setUpdatedTime(new Date());
        Example example = new Example(Orders.class);
        Example.Criteria criteria = example.createCriteria();
        //Order?????????orderId?????????????????????id??????????????????orderId
        criteria.andEqualTo("id", orderId);
        criteria.andEqualTo("userId", userId);
        int result = ordersMapper.updateByExampleSelective(updateOrder, example);

        return result == 1 ? true : false;
    }
    @Transactional(propagation=Propagation.SUPPORTS)
    @Override
    public OrderStatusCountsVO getOrderStatusCounts(String userId) {
        //????????????????????????????????????????????????4????????????
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        map.put("orderStatus",OrderStatusEnum.WAIT_PAY.type);
        int waitPayCounts = ordersMapperCustom.getMyOrderStatusCounts(map);
        map.put("orderStatus",OrderStatusEnum.WAIT_DELIVER.type);
        int waitDeliverCounts = ordersMapperCustom.getMyOrderStatusCounts(map);
        map.put("orderStatus",OrderStatusEnum.WAIT_RECEIVE.type);
        int waitReceiveCounts = ordersMapperCustom.getMyOrderStatusCounts(map);
        //???????????????????????????????????????????????????????????????????????????????????????
        map.put("orderStatus",OrderStatusEnum.SUCCESS.type);
        map.put("orderStatus",YesOrNo.NO.type);
        int waitCommentCounts = ordersMapperCustom.getMyOrderStatusCounts(map);
        OrderStatusCountsVO countsVO = new OrderStatusCountsVO(waitPayCounts,waitDeliverCounts,
                waitReceiveCounts,waitCommentCounts);
        return countsVO;
    }

    @Override
    public PagedGridResult getOrdersTrend(String userId, Integer page, Integer pageSize) {
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        PageHelper.startPage(page,pageSize);
        List<OrderStatus> list = ordersMapperCustom.getMyOrderTrend(map);
        return setterPagedGird(list,page);
    }

    @Override
    public IMOOCJSONResult checkUserOrder(String userId, String orderId) {
        Orders order = queryMyOrder(userId, orderId);
        if (order == null) {
            return IMOOCJSONResult.errorMsg("??????????????????");
        }
        return IMOOCJSONResult.ok(order);
    }

    //???????????????????????????????????????list??????????????????--->List<?>
    private PagedGridResult setterPagedGird(List<?> list,Integer page){
        PageInfo<?> pageList = new PageInfo<>(list);
        PagedGridResult gird = new PagedGridResult();
        gird.setPage(page);//????????????(?????????????????????????????????????????????????????????????????????)
        gird.setRows(list);//?????????
        gird.setTotal(pageList.getPages());//????????????
        gird.setRecords(pageList.getTotal());//?????????????????????
        return gird;
    }
    //????????????????????????ID?????????ID????????????
    @Transactional(propagation=Propagation.SUPPORTS)
    @Override
    public Orders queryMyOrder(String userId, String orderId) {
        Orders orders = new Orders();
        orders.setUserId(userId);
        orders.setId(orderId);
        orders.setIsDelete(YesOrNo.NO.type);
        return ordersMapper.selectOne(orders);
    }

}
