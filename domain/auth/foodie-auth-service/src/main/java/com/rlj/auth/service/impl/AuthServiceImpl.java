package com.rlj.auth.service.impl;

import com.rlj.auth.service.AuthService;
import com.rlj.auth.service.pojo.Account;
import com.rlj.auth.service.pojo.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.rlj.auth.service.pojo.AuthCode.SUCCESS;
import static com.rlj.auth.service.pojo.AuthCode.USER_NOT_FOUND;

/**
 * @author Renlingjie
 * @name
 * @date 2022-03-23
 */
@RestController
public class AuthServiceImpl implements AuthService {
    /**
     * (1)思路就是：
     * I、用户每次访问，都会被网关Gateway拦截，转发到foodie-auth-service中去做校验
     * II、然后校验用户是否携带Token，或者有但和userId不匹配，如果校验失败，则认为没有资格访问这个系统，
     *   返回枚举类AuthCode中定义的USER_NOT_FOUND
     *
     * (2)至于Token怎么来的：
     * I、是在用户首次登录且成功的时候创建
     * II、Token是根据登录请求中的userId由Jwt的加密算法生成，因此一个userId对应一个Token，之后用户访
     *   问该系统都会被拦截，校验userId和Token是否对应的上，这样即使外界知道一个用户名，但是没有Token
     *   也同样访问不了
     *
     * (3)鉴权的意义何在：
     *   假设不做鉴权，恶意攻击者只需要知道一个方法的路由，就可以获取这个方法返回的信息，或者通过这个方法
     * 打出很高的QPS，所以有了鉴权，来自未登录用户的请求，统统认为非法，直接在网关层拦截
     *
     * (4)可以优化的地方
     *   因为这里使用Jwt中的对称加密，所以加密解密都需要到foodie-auth-service中去完成，那么每次用户请求
     * 过来，为了验证Token是否合法，都需要先通过Gateway将请求转发到foodie-auth-service的verifyToken
     * 方法中做校验，没问题啦再转发到请求路由对应的方法中。
     *   我们可以采用非对称加密，在foodie-auth-service中采用公钥加密的方式生成Token，之后的verify下放到
     * Gateway中，之后请求直接在Gateway中通过私钥解密，这样做有2点好处：
     * I、非对称加密安全性好
     * II、只有在用户首次登录获取加密Token的时候需要转发到foodie-auth-service中，其他的时候请求的身份校验
     *   直接就在Gateway中完成，省去了一次HTTP通讯(节约用时20～30ms)
     */

    @Autowired
    private JwtService jwtService;

    @Override
    public AuthResponse generateToken(String userId) {
        Account account = Account.builder().userId(userId).build();
        String token = jwtService.generateToken(account);
        account.setToken(token);

        return AuthResponse.builder().account(account).code(SUCCESS.getCode()).build();
    }

    @Override
    public AuthResponse verifyToken(Account account) {
        boolean isSuccess = jwtService.verify(account.getToken(), account.getUserId());
        return AuthResponse.builder().code(isSuccess ? SUCCESS.getCode() : USER_NOT_FOUND.getCode()).build();
    }

}
