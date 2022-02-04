package com.rlj.order.pojo.bo;

import com.rlj.pojo.ShopcartBO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Renlingjie
 * @name 下单BO，原来创建订单需要List<ShopcartBO> shopcartList, SubmitOrderBO submitOrderBO，在这里进行二合一
 * @date 2022-02-01
 */
@Data
@NoArgsConstructor   //无参构造
@AllArgsConstructor  //有参构造
public class PlaceOrderBO {
    private SubmitOrderBO order;
    private List<ShopcartBO> items;
}
