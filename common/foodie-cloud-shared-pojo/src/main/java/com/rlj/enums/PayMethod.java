package com.rlj.enums;

/**
 * 支付方式枚举
 */
public enum PayMethod {
    WEIXIN(1,"微信"),
    ALIPAY(2,"支付宝");
    //枚举参数
    public final Integer type;
    public final String value;
    //枚举类构造方法
    PayMethod(Integer type, String value) {
        this.type = type;
        this.value = value;
    }
}
