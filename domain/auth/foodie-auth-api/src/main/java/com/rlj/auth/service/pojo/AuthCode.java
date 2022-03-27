package com.rlj.auth.service.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Renlingjie
 * @name
 * @date 2022-03-23
 */
@AllArgsConstructor
public enum AuthCode {
    SUCCESS(100L),                 // 鉴权成功
    USER_NOT_FOUND(103L),          // 用户不存在(账号或密码错误，即使账号存在也要这样提示，以便恶意访问者通过"密码错误"提示这个账号是没问题的)
    INVALID_CREDENTIAL(105L);      // 不合法的验证，比如Token不对

    @Getter
    private Long code;
}
