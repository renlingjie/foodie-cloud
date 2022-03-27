package com.rlj.user.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.rlj.auth.service.AuthService;
import com.rlj.auth.service.pojo.Account;
import com.rlj.auth.service.pojo.AuthCode;
import com.rlj.auth.service.pojo.AuthResponse;
import com.rlj.controller.BaseController;
import com.rlj.pojo.IMOOCJSONResult;
import com.rlj.pojo.ShopcartBO;
import com.rlj.user.UserApplicationConfig;
import com.rlj.user.pojo.Users;

import com.rlj.user.pojo.bo.UserBO;
import com.rlj.user.pojo.vo.UsersVO;
import com.rlj.user.service.UserService;
import com.rlj.utils.CookieUtils;
import com.rlj.utils.JsonUtils;
import com.rlj.utils.MD5Utils;
import com.rlj.utils.RedisOperator;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static com.rlj.auth.service.pojo.AuthCode.SUCCESS;

@Slf4j
@RestController
@RequestMapping("passport")
public class PassportController extends BaseController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisOperator redisOperator;

    @Autowired
    private UserApplicationConfig userApplicationConfig;

    @Autowired
    private AuthService authService;

    private static final String AUTH_HEADER = "Authorization";
    private static final String UID_HEADER = "rlj-user-id";


    //1、判断用户名是否存在
    @GetMapping("/usernameIsExist")
    //返回的是一个枚举类，用来枚举不同状态下不同返回值的内涵
    public IMOOCJSONResult usernameIsExit(@RequestParam String username) {//@RequestParam表示这是一种请求类型的参数而不是路径参数
        //1、判断用户名不能为空
        if (StringUtils.isBlank(username)) {
            //如果请求传过来的用户名为空，就返回状态码500对应的枚举类状态
            return IMOOCJSONResult.errorMsg("用户名不能为空");
        }
        //2、查找注册的用户名是否存在
        boolean isExist = userService.queryUsernameIsExist(username);
        if (isExist) {
            return IMOOCJSONResult.errorMsg("用户名已经存在");//如果用户名已经存在，就返回状态码500对应的枚举类状态
        }
        return IMOOCJSONResult.ok();//说明用户名不为空且数据库中没有该用户名，返回状态码200对应的枚举类状态
    }
    //2、创建用户
    @PostMapping("/regist")
    //返回的是一个枚举类，用来枚举不同状态下不同返回值的内涵   //cookie：也来一遍HttpServletRequest、HttpServletResponse
    public IMOOCJSONResult regist(@RequestBody UserBO userBO ,HttpServletRequest request,
                                  HttpServletResponse response) {//@RequestBody表示接收post请求中的请求体
        // 配置中心控制注册功能的开闭
        if (userApplicationConfig.isDisableRegistration()){
            return IMOOCJSONResult.errorMsg("注册中心已经关闭用户注册功能，请发送对应请求开启！");
        }

        String username = userBO.getUsername();
        String password = userBO.getPassword();
        String confirmPassword = userBO.getConfirmPassword();
        //上面是在前端进行的校验（ajax发过来的异步请求的校验），在我们保存数据库之前，也要再进行一次校验
        //2.1、判断用户名和密码是否为空
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)
                || StringUtils.isBlank(confirmPassword)){
            return IMOOCJSONResult.errorMsg("用户名或密码不能为空");
        }
        //2.2、查询用户名是否存在
        boolean isExist = userService.queryUsernameIsExist(username);
        if (isExist) {
            return IMOOCJSONResult.errorMsg("用户名已经存在");
        }
        //2.3、密码长度不能少于6位
        if (password.length() < 6){
            return IMOOCJSONResult.errorMsg("密码长度不能小于6");
        }
        //2.4、判断两次密码是否一致
        if (!password.equals(confirmPassword)){
            return IMOOCJSONResult.errorMsg("两次密码输入不一致");
        }
        //2.5、实现注册
        Users userResult = userService.createUser(userBO);
        //复制过来
        //userResult = setNullProperty(userResult);当我们定义了UsersVO，里面很多属性都删了，所以这个清空一些属性的操作就不需要了
        //CookieUtils.setCookie(request,response,"user", JsonUtils.objectToJson(userResult),true);

        //生成用户token，存入redis会话，这样后端Redis中就有我们的用户会话信息了
        //1、首先我们要用某种规则创建一个令牌，这里我们就使用UUID(唯一识别码)进行生成，起名的话就"redis_usr_token+用户ID"
        String uniqueToken = UUID.randomUUID().toString().trim();
        redisOperator.set(REDIS_USR_TOKEN+":"+userResult.getId(),uniqueToken);
        //2、之后就像Tomcat将jsessionId放到cookie中，这里我们将这个令牌也放到cookie中，可以直接存入，但是还有一种方法比较推荐，就是我们
        //之前吧User放入到了我们的cookie中，现在我们直接将User和Token封装到UsersVO中，然后将UsersVO放到我们的cookie中，那就一步到位了
        //所以创建了UsersVO，拷贝user的信息，同时将令牌信息也放进去
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userResult,usersVO);
        usersVO.setUserUniqueToken(uniqueToken);
        //3、将原先放入cookie的userResult替换为现在封装了userResult和token的usersVO
        CookieUtils.setCookie(request,response,"user",JsonUtils.objectToJson(usersVO),true);
        //从redis中同步购物车数据
        synchShopCartData(userResult.getId(),request,response);
        return IMOOCJSONResult.ok();
    }

    //注册登录成功后，同步cookie和redis中的购物车数据（具体逻辑分析看33-6.4）
    private void  synchShopCartData(String userId,HttpServletRequest request, HttpServletResponse response){
        //从redis中获取购物车
        String shopCartJsonRedis = redisOperator.get(FOODIE_SHOPCART + ":" + userId);
        //从cookie中获取购物车
        String shopCartStrCookie = CookieUtils.getCookieValue(request,FOODIE_SHOPCART,true);
        //redis为空，cookie不为空就覆盖redis，都为空就不管了
        if (StringUtils.isBlank(shopCartJsonRedis)){
            if (StringUtils.isNotBlank(shopCartStrCookie)){
                redisOperator.set(FOODIE_SHOPCART + ":" + userId,shopCartStrCookie);
            }
        }else {
            //redis不为空，cookie不为空，合并cookie和redis中数据（相同的以）cookie为主
            if (StringUtils.isNotBlank(shopCartStrCookie)){
                /**
                 * 均不为空的合并思想如下（涉及循环，两个json字符串都要转换为list）：
                 * 1、已经存在的，把cookie中对应的数量，覆盖redis
                 * 2、覆盖后cookie的这个商品放入待删除的list中
                 * 3、循环完成后，重复的商品已经全部存储到list中，且已经更新了redis，这个时候删除cookie对应的list部分
                 * 4、此时cookie和redis就没有交集了，两者合并
                 * 5、合并后的结果更新到cookie/redis中
                 */
                List<ShopcartBO> shopCartListRedis = JsonUtils.jsonToList(shopCartJsonRedis,ShopcartBO.class);
                List<ShopcartBO> shopCartListCookie = JsonUtils.jsonToList(shopCartStrCookie,ShopcartBO.class);
                List<ShopcartBO> pendingDelete = new ArrayList<>();//上面所说的待删除的list
                for (ShopcartBO redisShopCart:shopCartListRedis) {
                    String redisSpecId = redisShopCart.getSpecId();
                    for (ShopcartBO cookieShopCart:shopCartListCookie) {
                        String cookieSpecId = cookieShopCart.getSpecId();
                        if (redisSpecId.equals(cookieSpecId)){
                            redisShopCart.setBuyCounts(cookieShopCart.getBuyCounts());//cookie覆盖到redis
                            pendingDelete.add(cookieShopCart);//加入待删除list中
                        }
                    }
                }
                shopCartListCookie.removeAll(pendingDelete);//cookie删除list部分
                shopCartListRedis.addAll(shopCartListCookie);//合并
                CookieUtils.setCookie(request,response,FOODIE_SHOPCART,JsonUtils.objectToJson(shopCartListRedis),true);//更新到cookie
                redisOperator.set(FOODIE_SHOPCART + ":" + userId,JsonUtils.objectToJson(shopCartListRedis));
            }else {//cookie为空，redis覆盖cookie
                CookieUtils.setCookie(request,response,FOODIE_SHOPCART,shopCartJsonRedis,true);
            }
        }
    }

    //3、用户登录
    @PostMapping("/login")
    //开启降级保护，采用静默降级，即开启保护时不进入方法，直接返回一个指定页面。比如12306，我们验证码明明有时候是正确的，
    //但是还是提示错误，就是因为静默降级，根本没验证填写的验证码，直接返回错误提示页面
    @HystrixCommand(
            commandKey = "loginFail",  //全局唯一的标识服务，指定后可以在项目的yml文件中，通过这个commandKey，找到这个降级注解。不指定默认方法名(这里默认即login)
            groupKey = "password",  //全局的服务分组，一般用于统计、前面的统计仪表盘dashboard，默认该方法所在类的类名
            fallbackMethod = "loginFail",  //指定降级方法名，降级方法需要与该方法在同一个类中，且public/private修饰均可
            //hystrix的线程池、信号量等配置，这里配置Hystrix的线程池(关于线程池复习一下Tomcat为什么不用JDK原生线程池)。即线程隔离的真正实现
            //指定一个方法放到某个threadPoolKey对应的池子里，之后所有在此池子里的方法使用这个池子对应的线程，从而实现线程隔离(不同池子的方法，即使hang住，也不会
            //影响其他池子里方法的执行，这个是注解版。还有继承HystrixCommand的代码版)
            threadPoolKey = "threadPoolA",
            threadPoolProperties = {
                    @HystrixProperty(name = "coreSize",value = "20"),  //核心线程数
                    @HystrixProperty(name = "maxQueueSize",value = "40")  //核心线程数满后，请求放到阻塞队列的最大容量
            }
            //hystrix的配置属性，比如之前我们的超时配置在配置文件中，也可以下放到@HystrixCommand的commandProperties中配置
            //commandProperties = {}
    )
    //返回的是一个枚举类，用来枚举不同状态下不同返回值的内涵
    //CookieUtils方法要用到HttpServletRequest、HttpServletResponse，所以一并传入
    public IMOOCJSONResult login(@RequestBody UserBO userBO, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception{//@RequestBody表示接收post请求中的请求体
        String username = userBO.getUsername();
        String password = userBO.getPassword();
        //3.1、判断用户名和密码是否为空
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)){
            return IMOOCJSONResult.errorMsg("用户名或密码不能为空");
        }
        //3.2、实现登录(需要将将结果加密)
        Users userResult = userService.queryUserForLogin(username,
                MD5Utils.getMD5Str(password));
        if (userResult == null){//说明根据用户名密码在数据库查询不到这样的一个Users，返回的肯定是一个空集
            return IMOOCJSONResult.errorMsg("用户名或密码不正确");
        }

        //登录成功，生成鉴权的Token
        AuthResponse authResponse = authService.generateToken(userResult.getId());
        if (!AuthCode.SUCCESS.getCode().equals(authResponse.getCode())){
            log.error("Token error -- userId = {}",userResult.getId());
            return IMOOCJSONResult.errorMsg("用户Token错误！");
        }

        //将Token添加到Header中 TODO 再次提醒，在前端拿到Authorization、rlj-user-id，之后每次请求，把这两个参数带上
        response.setHeader(AUTH_HEADER, authResponse.getAccount().getToken());
        response.setHeader(UID_HEADER, authResponse.getAccount().getUserId());



        //同理注册的session同步
        String uniqueToken = UUID.randomUUID().toString().trim();
        redisOperator.set(REDIS_USR_TOKEN+":"+userResult.getId(),uniqueToken);
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userResult,usersVO);
        usersVO.setUserUniqueToken(uniqueToken);
        CookieUtils.setCookie(request,response,"user",JsonUtils.objectToJson(usersVO),true);

        //从redis中同步购物车数据
        synchShopCartData(userResult.getId(),request,response);
        return IMOOCJSONResult.ok(userResult);
    }

    //上面@HystrixCommand中指定的降级方法(能到降级方法中肯定是异常、超时等，我们可以添加一个Throwable参数去接收错误)
    private IMOOCJSONResult loginFail(UserBO userBO, HttpServletRequest request,
                                     HttpServletResponse response, Throwable throwable) throws Exception{
        return IMOOCJSONResult.errorMsg("登录失败！请重试");
    }


    //4、定义一个清空查询到的Users对象的私密属性的方法
    private Users setNullProperty(Users userResult){
        userResult.setPassword(null);
        userResult.setMobile(null);
        userResult.setEmail(null);
        userResult.setCreatedTime(null);
        userResult.setUpdatedTime(null);
        userResult.setBirthday(null);
        return userResult;
    }
    //5、用户登出
    @PostMapping("/logout")
    //来个swagger2
    @ApiOperation(value = "用户退出登录",notes = "用户退出登录",httpMethod = "POST")
    public IMOOCJSONResult logout(@RequestParam String userId, HttpServletRequest request,
                                  HttpServletResponse response){
        //清除用户相关的cookie
        CookieUtils.deleteCookie(request,response,"user");

        //用户退出登录，清除redis中user的会话信息
        redisOperator.del(REDIS_USR_TOKEN+":"+userId);

        //清除购物车相关的cookie
        CookieUtils.deleteCookie(request,response,FOODIE_SHOPCART);
        return IMOOCJSONResult.ok();
    }
}
