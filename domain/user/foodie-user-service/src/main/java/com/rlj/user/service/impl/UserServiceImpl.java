package com.rlj.user.service.impl;

import com.rlj.enums.Sex;
import com.rlj.user.mapper.UsersMapper;
import com.rlj.user.pojo.Users;
import com.rlj.user.pojo.bo.UserBO;
import com.rlj.user.service.UserService;
import com.rlj.utils.DateUtil;
import com.rlj.utils.MD5Utils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

@RestController
public class UserServiceImpl implements UserService {
    @Autowired
    public UsersMapper usersMapper;
    @Autowired
    private Sid sid;//自动装配用户唯一ID的生成策略类的对象
    //用户默认头像地址
    public static final String USER_FACE = "http://122.152.205.72:88/group1/M00/00/05/CpoxxFw_8_qAIlFXAAAcIhVPdSg994.png";
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryUsernameIsExist(String username) {
        //说明：usersMapper中有很多方法，比如说selectOne通过判断里面是否存在这样一个用户名来确定请求的注
        //册用户名是否可用，这里我们尝试另外的一个selectOneByExample（根据条件进行查询），看是否存在这样
        //的一种情况，里面传入一个Example（条件），根据这个条件来返回执行结果
        //1、创建Example，里面传入XXX.class，用来告诉是查询哪一个条件对应的实体类
        Example userExample = new Example(Users.class);
        //2、创建出条件对象
        Example.Criteria userExampleCriteria = userExample.createCriteria();
        //3、通过这个条件对象调用方法andEqualTo，表明这个条件对象的条件是"我们传进来的用户名去和我们数据库字
        // 段的用户名是否相等"
        userExampleCriteria.andEqualTo("username",username);//第一个参数名称是和pojo中的相同，而不是和数据库字段相同
        //4、给selectOneByExample传入我们上面指定条件的条件对象，根据数据库中是否存在一个与方法传入参数相等
        // 的内容，来返回true/false
        Users result = usersMapper.selectOneByExample(userExample);
        return result == null ? false : true;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users createUser(UserBO userBO) {
        String userId = sid.nextShort();//根据策略生成唯一ID
        Users users = new Users();
        users.setId(userId);//将唯一ID保存到用户对象中
        users.setUsername(userBO.getUsername());
        //password我们不能明文保存在数据中（防止数据库被黑客攻破），所以要加密保存在数据库中
        try {
            users.setPassword(MD5Utils.getMD5Str(userBO.getPassword()));//在这里进行加密，详情看6.2页面
        } catch (Exception e) {
            e.printStackTrace();
        }
        //默认用户昵称同用户名
        users.setNickname(userBO.getUsername());
        //默认头像
        users.setFace(USER_FACE);
        //默认生日（这里使用日期转换的工具类）
        users.setBirthday(DateUtil.stringToDate("1900-01-01"));
        //默认性别（为保密），这里使用枚举
        users.setSex(Sex.secret.type);
        //注册时间
        users.setCreatedTime(new Date());
        //更新时间
        users.setUpdatedTime(new Date());
        usersMapper.insert(users);//同样也是使用通用mapper的方法就可以实现用户的保存
        return users;//之所以还要将用户返回，主要是为了在页面中返回一些用户的基本信息
    }
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserForLogin(String username, String password) {
        //1、创建Example，里面传入XXX.class，用来告诉是查询哪一个条件对应的实体类
        Example userExample = new Example(Users.class);
        //2、创建出条件对象
        Example.Criteria userExampleCriteria = userExample.createCriteria();
        //3、通过这个条件对象调用方法andEqualTo，表明这个条件对象的条件是"我们传进来的用户名/密码去和我们数据库字
        // 段的用户名/密码是否相等"
        userExampleCriteria.andEqualTo("username",username);//第一个参数名称是和pojo中的相同，而不是和数据库字段相同
        userExampleCriteria.andEqualTo("password",password);
        //当条件定义好后，就可以将条件对应的实体类作为参数传入mapper中的selectOneByExample方法
        Users result = usersMapper.selectOneByExample(userExample);
        return result;
    }
}
