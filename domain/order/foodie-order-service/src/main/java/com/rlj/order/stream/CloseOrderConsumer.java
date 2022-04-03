package com.rlj.order.stream;

import com.rlj.enums.OrderStatusEnum;
import com.rlj.order.mapper.OrderStatusMapper;
import com.rlj.order.pojo.OrderStatus;
import com.rlj.order.pojo.bo.NeedCloseOrderBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * @author Renlingjie
 * @name
 * @date 2022-04-03
 */
@Slf4j
@EnableBinding(value = {CloseOrderChannel.class})
public class CloseOrderConsumer {

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @StreamListener(CloseOrderChannel.INPUT)
    public void consumeOrderStatusMessage(NeedCloseOrderBO bean){
        log.info("接收到关闭订单的请求，orderId={}",bean.getOrderId());

        //查询所有未付款订单，判断时间是否超时(1天)。超时则关闭交易
        OrderStatus queryOrder = new OrderStatus();
        queryOrder.setOrderId(bean.getOrderId());
        queryOrder.setOrderStatus(OrderStatusEnum.WAIT_PAY.type);
        List<OrderStatus> list = orderStatusMapper.select(queryOrder);

        if (CollectionUtils.isEmpty(list)){ // 说明这个订单已经关闭或者支付了才查询不出来
            log.info("订单已经支付或者关闭，orderId={}",bean.getOrderId());
            return;
        }

        // 走到这里说明要关闭的订单还是等待支付状态，已经过了30min，需要关闭了
        OrderStatus updateOrder = new OrderStatus();
        updateOrder.setOrderId(bean.getOrderId());
        updateOrder.setOrderStatus(OrderStatusEnum.CLOSE.type);
        updateOrder.setCloseTime(new Date());

        orderStatusMapper.updateByPrimaryKey(updateOrder);
        log.info("关闭了订单：orderId={}",bean.getOrderId());
    }
}
