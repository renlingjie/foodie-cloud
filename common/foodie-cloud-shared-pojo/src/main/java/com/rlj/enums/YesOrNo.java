package com.rlj.enums;

/**
 * 是否枚举
 */
public enum YesOrNo {
    NO(0,"否"),
    YES(1,"是");
    //枚举参数
    public final Integer type;
    public final String value;
    //枚举类构造方法
    YesOrNo(Integer type, String value) {
        this.type = type;
        this.value = value;
    }
}
