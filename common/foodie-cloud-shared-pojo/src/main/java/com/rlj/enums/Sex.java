package com.rlj.enums;

/**
 * 性别枚举
 */
public enum Sex {
    woman(0,"女"),
    man(1,"男"),
    secret(2,"保密");
    //枚举参数
    public final Integer type;
    public final String value;
    //枚举类构造方法
    Sex(Integer type, String value) {
        this.type = type;
        this.value = value;
    }
}
