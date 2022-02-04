package com.rlj.order.controller;

import com.rlj.controller.BaseController;
import com.rlj.enums.OrderStatusEnum;
import com.rlj.enums.PayMethod;
import com.rlj.order.pojo.OrderStatus;
import com.rlj.order.pojo.bo.PlaceOrderBO;
import com.rlj.order.pojo.bo.SubmitOrderBO;
import com.rlj.order.pojo.vo.MerchantOrdersVO;
import com.rlj.order.pojo.vo.OrderVO;
import com.rlj.order.service.OrderService;
import com.rlj.pojo.IMOOCJSONResult;
import com.rlj.pojo.ShopcartBO;
import com.rlj.utils.CookieUtils;
import com.rlj.utils.JsonUtils;
import com.rlj.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Api(value = "订单相关",tags = {"订单相关的api接口"})
@RequestMapping("orders")
@RestController//该注解让返回的所有请求都是json对象
public class OrdersController extends BaseController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private RedisOperator redisOperator;


    //1、创建订单
    //刷新购物车中的数据(主要是商品价格)
    @ApiOperation(value = "用户下单", notes = "用户下单", httpMethod = "POST")
    @PostMapping("/create")
    //前端的那个请求（serverUrl+'/orders/create,'+{xxxxxx}）参数是json，所以@RequestBody
    public IMOOCJSONResult create(@RequestBody SubmitOrderBO submitOrderBO,
                                  HttpServletRequest request, HttpServletResponse response) {
        //使用Redisson前唯一要做的事情---配置Redisson(Redisson的Config)，完成后就可以创建Redisson客户端实例
        Config config = new Config();
        //这里我们只启动一个Redis，所以使用SingleServer
        config.useSingleServer().setAddress("redis://localhost:6379");
        RedissonClient redissonClient = Redisson.create(config);
        /**
         * 二、用户下单请求的时候校验Redis中是否有此请求的Token，有两种情况需要考虑：
         *  1、如果校验成功，应该将这个Token删除，因为如果不删除，第二次、第三次还会通过验证，幂等性失效
         *  2、并发的时候，在第一次校验通过，Redis要删除Token前，第二次也通过校验，还是会产生两个订单。所以加分布式锁，
         *     保证线程安全，从而保证高并发情况下的幂等性不受影响
         */
        String orderTokenKey = "ORDER_TOKEN_"+request.getSession().getId();
        String lockKey = "LOCK_KEY_"+request.getSession().getId();
        RLock lock = redissonClient.getLock(lockKey);//上锁，第二个重复请求就会在这等待锁，就不会有并发的哪种情况了
        lock.lock(5, TimeUnit.SECONDS);//加锁，创建订单5s怎么说都够了
        try {
            String orderToken = redisOperator.get(orderTokenKey);
            if (StringUtils.isBlank(orderToken)) throw new RuntimeException("orderToken不存在");
            boolean currentToken = orderToken.equals(submitOrderBO.getToken());
            if (!currentToken) throw new RuntimeException("orderToken不正确");
            redisOperator.del(orderTokenKey);
        }finally {
            //无论结果如何，都释放锁，但是需注意一个问题，上面设置锁的过期时间为5s，虽说下面创建订单一般是够用了，
            //但是有时候不够用时，上面锁过期自动释放，再到这里手动释放就会找不到锁，就报错，故再try/catch一层
            try{
                lock.unlock();
            } catch (Exception e){
                //捕捉到释放锁的异常，就不往外抛了
            }
        }

        if (submitOrderBO.getPayMethod() != PayMethod.WEIXIN.type
        && submitOrderBO.getPayMethod() != PayMethod.ALIPAY.type){
            return IMOOCJSONResult.errorMsg("支付方式不支持");
        }

        String shopCartJson = redisOperator.get(FOODIE_SHOPCART + ":" + submitOrderBO.getUserId());
        if (StringUtils.isBlank(shopCartJson)){
            return IMOOCJSONResult.errorMsg("购物车数据不正确");
        }
        List<ShopcartBO> shopcartList = JsonUtils.jsonToList(shopCartJson,ShopcartBO.class);

        //1、创建订单
        PlaceOrderBO placeOrderBO = new PlaceOrderBO(submitOrderBO,shopcartList);
        OrderVO orderVO = orderService.createOrder(placeOrderBO);
        String orderId = orderVO.getOrderId();
        //2、创建订单以后，移除购物车中已结算的商品
        //整合Redis后，完善购物车中的已结算商品清除，并且同步到前端的Cookie
        //将已经结算的商品从我们的购物车Redis中清除，清除后的shopcartList就是最新的，更新到Redis中
        shopcartList.removeAll(orderVO.getToBeRemovedShopcartList());
        redisOperator.set(FOODIE_SHOPCART + ":" + submitOrderBO.getUserId(),JsonUtils.objectToJson(shopcartList));
        //使用上面最新的shopcartList更新Cookie
        CookieUtils.setCookie(request,response,FOODIE_SHOPCART,JsonUtils.objectToJson(shopcartList),true);
        //3、向支付中心发送当前订单，用于保存支付中心的订单数据
        MerchantOrdersVO merchantOrdersVO = orderVO.getMerchantOrdersVO();
        merchantOrdersVO.setReturnUrl(payReturnUrl);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add("imoocUserId","imooc");
        httpHeaders.add("password","imooc");
        //要传入的参数(一个是封装了订单的的merchantOrdersVO，一个是支付中心的账号密码httpHeaders)
        HttpEntity<MerchantOrdersVO> entity = new HttpEntity<>(merchantOrdersVO,httpHeaders);
        //三个参数：支付中心对应的路由、要传给支付中心的路由、返回的类型，这样就通过restTemplate变为RestFul风格请求
        ResponseEntity<IMOOCJSONResult> responseEntity =
                restTemplate.postForEntity(paymentUrl, entity, IMOOCJSONResult.class);
        //拿到支付中心返回的结果
        IMOOCJSONResult paymentResult = responseEntity.getBody();
        if (paymentResult.getStatus() != 200){
            return IMOOCJSONResult.errorMsg("支付中心订单创建失败，请联系订单管理员");
        }
        return IMOOCJSONResult.ok(orderId);
    }

    //构建商户端支付成功的回调接口
    @PostMapping("notifyMerchantOrderPaid")
    public Integer notifyMerchantOrderPaid(String merchantOrderId){
        //传入要修改状态的订单的ID，同时将订单状态设置为已付款待发货
        System.out.println("回调接口传入的ID是"+merchantOrderId);
        orderService.updateOrderStatus(merchantOrderId, OrderStatusEnum.WAIT_DELIVER.type);
        return HttpStatus.OK.value();//直接返回200状态码
    }

    //返回前端订单状态的接口
    @PostMapping("getPaidOrderInfo")
    public IMOOCJSONResult queryOrderStatusInfo(String orderId){
        OrderStatus orderStatus = orderService.queryOrderStatusInfo(orderId);
        //System.out.println("接收到请求，订单状态为"+orderStatus);
        return IMOOCJSONResult.ok(orderStatus);
    }

    /**
     * 一、生成Token
     * 1、直接UUID生成Token，保证其唯一
     * 2、之后将Token存入Redis，以便后续前端请求过来的Token与Redis中的Token对比
     * 3、存入Redis中的这个Token的key需要考虑：主要考虑的是幂等性的力度，比如，现在用户在两个浏览器(即两个会话)下单，
     *    会认为是两个不同的下单操作。即幂等性只防一个浏览器，这个时候不同浏览器的key需要不同，不同浏览器下单即开启了
     *    不同的会话，所以我们请求的参数可以是HttpSession，通过SessionID构造key，最终实现"同一个浏览器要求幂等，
     *    不同浏览器不要求幂等"的需求
     * 4、Token要设置一个过期时间后自动释放，因为用户到下单页，前端发起请求生成Token，但此时用户有一段时间没有支付，
     *    我们不能直接就失效了，而是设置一个过期时间，超过这个时间用户再支付才会失效(比如下面的10min)
     */
    @ApiOperation(value = "获取订单Token", notes = "获取订单Token", httpMethod = "POST")
    @PostMapping("/getOrderToken")
    public IMOOCJSONResult getOrderToken(HttpSession session){
        String token = UUID.randomUUID().toString();
        redisOperator.set("ORDER_TOKEN_"+session.getId(),token,600);
        return IMOOCJSONResult.ok(token);
    }
}
