package com.rlj.user.controller;

import com.rlj.pojo.IMOOCJSONResult;
import com.rlj.user.pojo.UserAddress;
import com.rlj.user.pojo.bo.AddressBO;
import com.rlj.user.service.AddressService;

import com.rlj.utils.MobileEmailUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "地址相关",tags = {"地址相关的api接口"})
@RequestMapping("address")
@RestController//该注解让返回的所有请求都是json对象
public class AddressController {
    @Autowired
    private AddressService addressService;
    //1、查询用户的所有收货地址列表
    //刷新购物车中的数据(主要是商品价格)
    @ApiOperation(value = "根据用户id查询收货地址列表", notes = "根据用户id查询收货地址列表", httpMethod = "POST")
    @PostMapping("/list")
    //前端的那个请求（serverUrl+'/address/list?usercId='+userId,{}）
    //这里的参数都是请求参数，而不是像之前是路径参数，所以这里@PathVariable--->@RequestParam
    public IMOOCJSONResult refresh(@RequestParam String userId) {
        if (StringUtils.isBlank(userId)) {
            return IMOOCJSONResult.errorMsg("");
        }
        List<UserAddress> list = addressService.queryAll(userId);
        return IMOOCJSONResult.ok(list);
    }
    //2、新增收货地址
    //刷新购物车中的数据(主要是商品价格)
    @ApiOperation(value = "新增收货地址", notes = "新增收货地址", httpMethod = "POST")
    @PostMapping("/add")
    //前端的那个请求（serverUrl+'/address/add',{}），后面是一个json，所以这里@RequestBody
    public IMOOCJSONResult add(@RequestBody AddressBO addressBO) {
        IMOOCJSONResult checkRes = checkAddress(addressBO);
        if (checkRes.getStatus() != 200) {
            return checkRes;
        }
        addressService.addNewUserAddress(addressBO);
        return IMOOCJSONResult.ok();
    }
    //定义一个判断前端传入参数的方法
    private IMOOCJSONResult checkAddress(AddressBO addressBO) {
        String receiver = addressBO.getReceiver();
        if (StringUtils.isBlank(receiver)) {
            return IMOOCJSONResult.errorMsg("收货人不能为空");
        }
        if (receiver.length() > 12) {
            return IMOOCJSONResult.errorMsg("收货人姓名不能太长");
        }

        String mobile = addressBO.getMobile();
        if (StringUtils.isBlank(mobile)) {
            return IMOOCJSONResult.errorMsg("收货人手机号不能为空");
        }
        if (mobile.length() != 11) {
            return IMOOCJSONResult.errorMsg("收货人手机号长度不正确");
        }
        boolean isMobileOk = MobileEmailUtils.checkMobileIsOk(mobile);
        if (!isMobileOk) {
            return IMOOCJSONResult.errorMsg("收货人手机号格式不正确");
        }

        String province = addressBO.getProvince();
        String city = addressBO.getCity();
        String district = addressBO.getDistrict();
        String detail = addressBO.getDetail();
        if (StringUtils.isBlank(province) ||
                StringUtils.isBlank(city) ||
                StringUtils.isBlank(district) ||
                StringUtils.isBlank(detail)) {
            return IMOOCJSONResult.errorMsg("收货地址信息不能为空");
        }

        return IMOOCJSONResult.ok();
    }

    //3、修改收货地址
    @ApiOperation(value = "用户修改地址", notes = "用户修改地址", httpMethod = "POST")
    @PostMapping("/update")
    //前端的那个请求（serverUrl+'/address/update',{}），后面是一个json，所以这里@RequestBody
    public IMOOCJSONResult update(@RequestBody AddressBO addressBO) {
        if (StringUtils.isBlank(addressBO.getAddressId())) {
            return IMOOCJSONResult.errorMsg("修改地址错误：addressId不能为空");
        }
        IMOOCJSONResult checkRes = checkAddress(addressBO);
        if (checkRes.getStatus() != 200) {
            return checkRes;
        }
        addressService.updateUserAddress(addressBO);
        return IMOOCJSONResult.ok();
    }
    //4、删除收货地址
    @ApiOperation(value = "用户删除地址", notes = "用户删除地址", httpMethod = "POST")
    //前端的那个请求（serverUrl+'/address/delete?userId'+userInfo.id+'&addressId'+addressId,{}）
    //这里的参数都是请求参数，而不是像之前是路径参数，所以这里@PathVariable--->@RequestParam
    @PostMapping("/delete")
    public IMOOCJSONResult delete(
            @RequestParam String userId,
            @RequestParam String addressId) {
        //虽然为空数据库也就不会删除什么，但是我们拦截一下，减缓数据库的压力，也防止恶意攻击
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(addressId)) {
            return IMOOCJSONResult.errorMsg("");
        }

        addressService.deleteUserAddress(userId, addressId);
        return IMOOCJSONResult.ok();
    }
    //5、设置默认地址
    @ApiOperation(value = "用户设置默认地址", notes = "用户设置默认地址", httpMethod = "POST")
    //前端的那个请求（serverUrl+'/address/setDefault?userId'+userInfo.id+'&addressId'+addressId,{}）
    //这里的参数都是请求参数，而不是像之前是路径参数，所以这里@PathVariable--->@RequestParam
    @PostMapping("/setDefault")
    public IMOOCJSONResult setDefault(
            @RequestParam String userId,
            @RequestParam String addressId) {

        if (StringUtils.isBlank(userId) || StringUtils.isBlank(addressId)) {
            return IMOOCJSONResult.errorMsg("");
        }
        addressService.updateUserDefaultAddress(userId, addressId);
        return IMOOCJSONResult.ok();
    }
}
