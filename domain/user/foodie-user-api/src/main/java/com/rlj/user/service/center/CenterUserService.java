package com.rlj.user.service.center;

import com.rlj.user.pojo.Users;
import com.rlj.user.pojo.bo.center.CenterUserBO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("foodie-user-service")
@RequestMapping("center-user-api")
public interface CenterUserService {
    //1、根据用户ID查询用户信息
    @GetMapping("profile")
    public Users queryUserInfo(@RequestParam("userId")String userId);

    //2、修改用户信息
    @PutMapping("profile/{userId}")
    public Users updateUserInfo(@PathVariable("userId")String userId,
                                @RequestBody CenterUserBO centerUserBO);
    //3、用户头像更新
    @PostMapping("updatePhoto")
    public Users updateUserFace(@RequestParam("userId")String userId, @RequestParam("faceUrl")String faceUrl);
}
