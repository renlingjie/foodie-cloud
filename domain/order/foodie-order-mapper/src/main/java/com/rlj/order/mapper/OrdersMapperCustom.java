package com.rlj.order.mapper;

import com.rlj.order.pojo.OrderStatus;
import com.rlj.order.pojo.vo.MyOrdersVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface OrdersMapperCustom {
    //因为请求参数有多个，所以我们用map封装，同时在后面的xml中，parameterType也是map这里
    //给map起了别名paramsMap，会在后面的xml中使用，查询的一个订单里的商品可能有多个，所以是List
    public List<MyOrdersVO> queryMyOrders(@Param("paramsMap") Map<String,Object> map);
    //根据订单的某种状态查询该状态下的订单数量(因为要传userId、orderStatus，所以用map)
    public int getMyOrderStatusCounts(@Param("paramsMap") Map<String,Object> map);
    //根据订单的20/30/40状态查询在这些状态下订单的动向(其实只传userId一个参数，但是用map也可以)
    public List<OrderStatus> getMyOrderTrend(@Param("paramsMap") Map<String,Object> map);
}
