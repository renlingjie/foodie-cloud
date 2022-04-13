package com.rlj.controller;

import org.springframework.stereotype.Controller;

import java.io.File;

@Controller
public class BaseController {
    //小写转换成大写：command+shift+U
    public static final Integer COMMENT_PAGE_SIZE = 10;
    //默认搜索界面的展示记录书为20
    public static final Integer Search_PAGE_SIZE = 20;
    //定义我们的Cookie的名字，因为前端就叫shopcart，所以这里也要保持一致
    public static final String FOODIE_SHOPCART = "shopcart";
    //定义一个Redis的token名称
    public static final String REDIS_USR_TOKEN = "redis_usr_token";
    //支付成功前往后端支付成功接口的地址
    // FIXME 回调接口，这个老师没说，既然是OrdersController的notifyMerchantOrderPaid，所以改一下
    // 请求Order模块，直接用Gateway，会拦截/orders/**，之后去请求foodie-order-service服务下的对应方法
    public String payReturnUrl = "http://152.136.220.204:20004/orders/notifyMerchantOrderPaid";
    //public String payReturnUrl = "http://8.141.50.179:8088/foodMart-api/orders/notifyMerchantOrderPaid";
    // 支付中心的调用地址
    public String paymentUrl = "http://payment.t.mukewang.com/foodie-payment/payment/createMerchantOrder";
    //用户上传头像的位置(File.separator等价于"/")
    public static final String IMAGE_USER_FACE_LOCATION = File.separator + "workspaces" +
            File.separator + "images" +
            File.separator + "foodie" +
            File.separator + "faces";

//    public static final String IMAGE_USER_FACE_LOCATION = File.separator + "Users" +
//                                                          File.separator + "renlingjie" +
//                                                          File.separator + "Documents" +
//                                                          File.separator + "upLoadPicture";
}
