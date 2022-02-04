package com.rlj.user.service;

import com.rlj.user.pojo.UserAddress;
import com.rlj.user.pojo.bo.AddressBO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@FeignClient("foodie-user-service")
@RequestMapping("address-api")
public interface AddressService {
    //1、查询用户的所有收货地址列表
    @GetMapping("addressList")
    public List<UserAddress> queryAll(@RequestParam("userId") String userId);
    //2、用户新增地址
    @PostMapping("address")
    public void addNewUserAddress(@RequestBody AddressBO addressBO);
    //3、用户修改地址
    @PutMapping("address")
    public void updateUserAddress(@RequestBody AddressBO addressBO);
    //4、用户删除地址
    @DeleteMapping("address")
    public void deleteUserAddress(@RequestParam("userId")String userId,@RequestParam("addressId")String addressId);
    //5、修改默认地址
    @PostMapping("setDefaultAddress")
    public void updateUserDefaultAddress(@RequestParam("userId")String userId,@RequestParam("addressId")String addressId);
    //6、根据用户ID和地址ID（查询全部地址只需要用户ID），查询出前端用户指定的地址信息
    @GetMapping("queryAddress")
    public UserAddress queryUserAddress(@RequestParam("userId")String userId,
                                        @RequestParam(value = "addressId",required = false)String addressId);
}
