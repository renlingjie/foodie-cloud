package com.rlj.order.service;

import com.rlj.order.pojo.OrderStatus;
import com.rlj.order.pojo.bo.PlaceOrderBO;
import com.rlj.order.pojo.bo.SubmitOrderBO;
import com.rlj.order.pojo.vo.OrderVO;
import com.rlj.pojo.ShopcartBO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@FeignClient("foodie-order-service")
@RequestMapping("order-api")
public interface OrderService {
    //1、创建一个订单，并将前端需要的生成的订单ID返回，所以返回值是String
    @PostMapping("/placeOrder")
    public OrderVO createOrder(@RequestBody PlaceOrderBO orderBO);

    //2、接收到支付中心付款成功的消息后，需要将订单状态改为已付款
    @PostMapping("/updateStatus")
    public void updateOrderStatus(@RequestParam("orderId") String orderId,
                                  @RequestParam("orderStatus") Integer orderStatus);

    //3、查询订单状态
    @GetMapping("orderStatus")
    public OrderStatus queryOrderStatusInfo(@RequestParam("orderId")String orderId);

    //4、关闭超时未支付订单
    @PostMapping("/closePendingOrders")
    public void closeOrder();
}
