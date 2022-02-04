package com.rlj.enums;

//商品评价等级的枚举
public enum CommentLevel {
    GOOD(1,"好评"),
    NORMAL(2,"中评"),
    BAD(3,"差评");
    //枚举参数
    public final Integer type;
    public final String value;
    //枚举类构造方法
    CommentLevel(Integer type, String value) {
        this.type = type;
        this.value = value;
    }
}
