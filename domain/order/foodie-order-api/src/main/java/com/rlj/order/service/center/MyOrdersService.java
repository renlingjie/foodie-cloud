package com.rlj.order.service.center;

import com.rlj.order.pojo.Orders;
import com.rlj.order.pojo.vo.OrderStatusCountsVO;
import com.rlj.pojo.IMOOCJSONResult;
import com.rlj.pojo.PagedGridResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
@FeignClient("foodie-order-service")
@RequestMapping("myorder-api")
public interface MyOrdersService {
    //1、查询我的订单列表
    @GetMapping("order/query")
    public PagedGridResult queryMyOrders(@RequestParam("orderId")String userId,
                                         @RequestParam("orderStatus") Integer orderStatus,
                                         @RequestParam(value = "page",required = false)Integer page,
                                         @RequestParam(value = "pageSize",required = false) Integer pageSize);

    //2、更改订单状态为商家发货
    @PostMapping("order/delivered")
    public void updateDeliverOrderStatus(@RequestParam("orderId")String orderId);

    //3、更改订单状态为确认收货
    @PostMapping("order/received")
    public boolean updateReceiveOrderStatus(@RequestParam("orderId")String orderId);

    //4、检查订单ID和用户ID是否关联(如果根据这两个参数查出来了说明有关联)，防止恶意攻击
    @GetMapping("order/details")
    public Orders queryMyOrder(@RequestParam("userId")String userId, @RequestParam("orderId")String orderId);

    //5、删除订单(逻辑删除)
    @DeleteMapping("order")
    public boolean deleteOrder(@RequestParam("userId")String userId, @RequestParam("orderId")String orderId);

    //6、获取订单状态的计数
    @GetMapping("order/counts")
    public OrderStatusCountsVO getOrderStatusCounts(@RequestParam("userId")String userId);

    //7、获取订单状态为20/30/40的动向(要分页)
    @GetMapping("order/trend")
    public PagedGridResult getOrdersTrend(@RequestParam("userId")String userId,
                                          @RequestParam(value = "page",required = false)Integer page,
                                          @RequestParam(value = "pageSize",required = false)Integer pageSize);

    //8、验证用户和订单是否有关联关系，避免非法用户调用
    @GetMapping("checkUserOrder")
    public IMOOCJSONResult checkUserOrder(@RequestParam("userId")String userId,
                                          @RequestParam("orderId")String orderId);
}
