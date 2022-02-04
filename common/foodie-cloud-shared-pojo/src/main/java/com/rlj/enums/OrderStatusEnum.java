package com.rlj.enums;

/**
 * 支付状态枚举
 */
public enum OrderStatusEnum {
    WAIT_PAY(10,"待付款"),
    WAIT_DELIVER(20,"已付款，待发货"),
    WAIT_RECEIVE(30,"已发货，待收货"),
    SUCCESS(40,"交易成功"),
    CLOSE(50,"交易关闭");
    //枚举参数
    public final Integer type;
    public final String value;
    //枚举类构造方法
    OrderStatusEnum(Integer type, String value) {
        this.type = type;
        this.value = value;
    }
}
