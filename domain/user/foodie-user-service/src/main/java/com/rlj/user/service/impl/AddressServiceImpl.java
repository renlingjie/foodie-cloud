package com.rlj.user.service.impl;


import com.rlj.enums.YesOrNo;
import com.rlj.user.mapper.UserAddressMapper;
import com.rlj.user.pojo.UserAddress;
import com.rlj.user.pojo.bo.AddressBO;
import com.rlj.user.service.AddressService;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;


import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@RestController
public class AddressServiceImpl implements AddressService {
    @Autowired
    private UserAddressMapper userAddressMapper;
    @Autowired
    private Sid sid;
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<UserAddress> queryAll(String userId) {
        UserAddress ua = new UserAddress();
        ua.setUserId(userId);

        return userAddressMapper.select(ua);
    }
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void addNewUserAddress(AddressBO addressBO) {
        //1、判断当前用户是否存在地址，如果没有，则新增为默认地址
        Integer isDefault = 0;
        List<UserAddress> addressList = this.queryAll(addressBO.getUserId());
        if (addressList == null || addressList.isEmpty() || addressList.size() == 0){
            isDefault = 1;
        }
        //上面我们也自动装配了Sid，它是我们主键的生成策略，我们用它来给我们生成唯一的地址ID
        String addressId = sid.nextShort();
        //2、保存地址到数据库
        UserAddress newAddress = new UserAddress();
        //2.1、我们点进去发现AddressBO的属性UserAddress都有，所以传入的AddressBO本来可以通过get获取到属
        //性再set给UserAddress，但是这样太麻烦了，下面使用一工具类，可以将某对象属性拷贝另一对象的相同属性中
        BeanUtils.copyProperties(addressBO,newAddress);
        //2.2、除了传进来的参数补充进来后，其他要补全的属性：addressId(由Sid生成)、默认地址、创建时间、更新时间
        newAddress.setId(addressId);
        newAddress.setIsDefault(isDefault);
        newAddress.setCreatedTime(new Date());//创建之初创建时间和更新时间相同
        newAddress.setUpdatedTime(new Date());
        userAddressMapper.insert(newAddress);
    }
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateUserAddress(AddressBO addressBO) {
        //这就不用像上面通过Sid生成了，传入的就有。前面也说了，update函数比add函数的json中多了这个
        String addressId = addressBO.getAddressId();
        UserAddress updateAddress = new UserAddress();
        BeanUtils.copyProperties(addressBO,updateAddress);
        //除了传进来的参数补充进来后，其他要补全的属性：addressId(前端传入)、更新时间
        updateAddress.setId(addressId);
        updateAddress.setUpdatedTime(new Date());
        //updateByPrimaryKeySelective:根据主键进行更新
        userAddressMapper.updateByPrimaryKeySelective(updateAddress);
    }
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deleteUserAddress(String userId, String addressId) {
        UserAddress address = new UserAddress();
        address.setId(addressId);
        address.setUserId(userId);
        //方法中有一个是通过对象进行删除
        userAddressMapper.delete(address);
    }
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateUserDefaultAddress(String userId, String addressId) {
        //1、查找默认地址，设置为不默认
        //1.1、里面的方法select也是根据UserAddress对象查询的，所以我们要查询的条件
        //"该用户ID对应的地址中是默认地址的那个地址"，所以就要将这两个条件传入
        UserAddress queryAddress = new UserAddress();
        queryAddress.setUserId(userId);
        queryAddress.setIsDefault(YesOrNo.YES.type);
        List<UserAddress> list = userAddressMapper.select(queryAddress);
        //1.2、说实话这里list中肯定只有一个默认的，所以for循环可以不用，直接list.get(0)也行但是
        //这里用for是害怕数据紊乱，有多个是1的默认地址，我们都给他设置为非默认的0，相当于一层保险
        for (UserAddress ua : list){
            ua.setIsDefault(YesOrNo.NO.type);
            userAddressMapper.updateByPrimaryKeySelective(ua);
        }
        //2、根据地址ID修改为默认地址
        UserAddress defaultAddress = new UserAddress();
        defaultAddress.setId(addressId);
        defaultAddress.setUserId(userId);
        defaultAddress.setIsDefault(YesOrNo.YES.type);
        userAddressMapper.updateByPrimaryKeySelective(defaultAddress);
    }
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public UserAddress queryUserAddress(String userId, String addressId) {
        UserAddress singleAddress = new UserAddress();
        singleAddress.setId(addressId);
        singleAddress.setUserId(userId);
        return userAddressMapper.selectOne(singleAddress);
    }

}
