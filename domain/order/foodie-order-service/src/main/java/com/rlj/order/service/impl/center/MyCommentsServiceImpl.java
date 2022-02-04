package com.rlj.order.service.impl.center;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rlj.enums.YesOrNo;
import com.rlj.item.pojo.vo.MyCommentVO;
import com.rlj.item.service.ItemCommentsService;
import com.rlj.order.mapper.*;
import com.rlj.order.pojo.OrderItems;
import com.rlj.order.pojo.OrderStatus;
import com.rlj.order.pojo.Orders;
import com.rlj.order.pojo.bo.center.OrderItemsCommentBO;

import com.rlj.order.service.center.MyCommentsService;

import com.rlj.pojo.PagedGridResult;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MyCommentsServiceImpl implements MyCommentsService {

    @Autowired
    public OrderItemsMapper orderItemsMapper;

    @Autowired
    public OrdersMapper ordersMapper;

    @Autowired
    public OrderStatusMapper orderStatusMapper;

    // 将依赖注入的api接口拿到，因为已经被@FeignClient改造，所以注入后直接调用即可
    @Autowired
    public ItemCommentsService itemCommentsService;

    @Autowired
    private Sid sid;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<OrderItems> queryPendingComment(String orderId) {
        OrderItems query = new OrderItems();
        query.setOrderId(orderId);
        return orderItemsMapper.select(query);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveComments(String orderId, String userId,
                             List<OrderItemsCommentBO> commentList) {

        //1. 保存评价:items_comments
        //1.1、将一个订单对应的多个商品的多个评论的list集合遍历，给每一个评论对象都通过Sid设置一个commentId
        for (OrderItemsCommentBO oic : commentList) {
            oic.setCommentId(sid.nextShort());
        }
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("commentList", commentList);
        itemCommentsService.saveComments(map);
        // 2. 修改订单表(orders)改已评价
        Orders order = new Orders();
        order.setId(orderId);
        order.setIsComment(YesOrNo.YES.type);
        ordersMapper.updateByPrimaryKeySelective(order);
        // 3. 修改订单状态表的留言时间:order_status
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCommentTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
    }
    //分页方法，传入的可能是各种list，所以不写死--->List<?>
    private PagedGridResult setterPagedGird(List<?> list, Integer page){
        PageInfo<?> pageList = new PageInfo<>(list);
        PagedGridResult gird = new PagedGridResult();
        gird.setPage(page);//当前页数(请求的第几页作为参数传进来了，这里也要返回回去)
        gird.setRows(list);//每行显示的内容
        gird.setTotal(pageList.getPages());//总页数
        gird.setRecords(pageList.getTotal());//总记录数
        return gird;
    }
}
