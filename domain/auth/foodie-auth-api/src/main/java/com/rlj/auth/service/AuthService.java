package com.rlj.auth.service;

import com.rlj.auth.service.pojo.Account;
import com.rlj.auth.service.pojo.AuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @author Renlingjie
 * @name
 * @date 2022-03-23
 */
@FeignClient("foodie-auth-service")
@RequestMapping("auth-service")
public interface AuthService {
    // 用户登录成功，则生成Token，将Token随着Response返回给前端
    @PostMapping("token")
    public AuthResponse generateToken(@RequestParam("userId") String userId);

    // 校验Token
    @PostMapping("verify")
    public AuthResponse verifyToken(@RequestBody Account account);
}
