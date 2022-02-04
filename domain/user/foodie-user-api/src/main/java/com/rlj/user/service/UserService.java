package com.rlj.user.service;

import com.rlj.user.pojo.Users;
import com.rlj.user.pojo.bo.UserBO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("foodie-user-service")
@RequestMapping("user-api")
public interface UserService {
    /**
     * 判断用户名是否存在
     */
    @GetMapping("user/exists")
    public boolean queryUsernameIsExist(@RequestParam("username")String username);
    /**
     * 创建用户
     */
    @PostMapping("user")
    public Users createUser(@RequestBody UserBO userBO);//userBO用来封装前端请求的用户名和密码
    /**
     * 用户登录(检索用户名和密码是否匹配)
     */
    @GetMapping("verify")
    public Users queryUserForLogin(@RequestParam("username")String username,@RequestParam("password")String password);
}
