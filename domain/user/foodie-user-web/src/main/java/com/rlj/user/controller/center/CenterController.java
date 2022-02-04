package com.rlj.user.controller.center;

import com.rlj.pojo.IMOOCJSONResult;
import com.rlj.user.pojo.Users;
import com.rlj.user.service.center.CenterUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "center-用户中心",tags = {"用户中心展示的api接口"})
@RequestMapping("center")
@RestController//该注解让返回的所有请求都是json对象
public class CenterController {
    @Autowired
    private CenterUserService centerUserService;
    @ApiOperation(value = "获取用户信息",notes = "获取用户信息",httpMethod = "GET")
    @GetMapping("userInfo")//前端center中查询用户信息的路由就是center/userInfo
    public IMOOCJSONResult userInfo(@RequestParam String userId){//请求类型的参数
        Users user = centerUserService.queryUserInfo(userId);
        return IMOOCJSONResult.ok(user);
    }
}
