package com.rlj.cart.service.impl;

import com.rlj.cart.service.CartService;
import com.rlj.pojo.ShopcartBO;
import com.rlj.utils.JsonUtils;
import com.rlj.utils.RedisOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.rlj.controller.BaseController.FOODIE_SHOPCART;

/**
 * @author Renlingjie
 * @name
 * @date 2022-02-01
 */
@RestController
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisOperator redisOperator;

    @Override
    public boolean addItemToCart(@RequestParam("userId") String userId,
                                 @RequestBody ShopcartBO shopcartBO) {
        //前端用户在登录的情况下，添加商品到购物车，会同时在后端同步购物车到redis缓存
        //给每一个用户的购物车都命名一个唯一的名字，格式为"shopcart:用户ID"
        String shopcartJson = redisOperator.get(FOODIE_SHOPCART + ":" + userId);
        List<ShopcartBO> shopcartList = null;
        if (StringUtils.isNotBlank(shopcartJson)) {
            //Redis中已经存在购物车了，这时需要判断购物车中商品与新添加的是否有相同的，如果是则进行累加
            shopcartList = JsonUtils.jsonToList(shopcartJson, ShopcartBO.class);
            // 判断购物车中是否存在已有商品，如果有的话counts累加
            boolean isHaving = false;
            for (ShopcartBO sc: shopcartList) {
                String tmpSpecId = sc.getSpecId();
                if (tmpSpecId.equals(shopcartBO.getSpecId())) {
                    sc.setBuyCounts(sc.getBuyCounts() + shopcartBO.getBuyCounts());
                    isHaving = true;
                }
            }
            if (!isHaving) {
                shopcartList.add(shopcartBO);
            }
        } else {
            // redis中没有购物车
            shopcartList = new ArrayList<>();
            // 直接添加到购物车中
            shopcartList.add(shopcartBO);
        }

        //编辑完要存储到Redis中的shopcartList后，我们就将这个shopcartList存储/更新到Redis中
        redisOperator.set(FOODIE_SHOPCART + ":" + userId, JsonUtils.objectToJson(shopcartList));

        return true;
    }

    @Override
    public boolean removeItemFromCart(@RequestParam("userId") String userId,
                                      @RequestParam("itemSpecId") String itemSpecId) {
        //用户在页面删除购物车中的数据，如果此时用户已经登录，则在后端同步删除redis缓存
        String shopcartJson = redisOperator.get(FOODIE_SHOPCART + ":" + userId);
        if (StringUtils.isNotBlank(shopcartJson)) {
            //Redis中已经存在购物车了，这时需要判断购物车中商品与要删除的是否有相同的，如果有的话则进行删除
            List<ShopcartBO> shopcartList = JsonUtils.jsonToList(shopcartJson, ShopcartBO.class);
            for (ShopcartBO sc: shopcartList) {
                String tmpSpecId = sc.getSpecId();
                if (tmpSpecId.equals(itemSpecId)) {
                    shopcartList.remove(sc);
                    break;
                }
            }
            //编辑完要存储到Redis中的shopcartList后，我们就将这个shopcartList更新到Redis中
            redisOperator.set(FOODIE_SHOPCART + ":" + userId, JsonUtils.objectToJson(shopcartList));
        }

        return true;
    }

    @Override
    public boolean clearCart(@RequestParam("userId") String userId) {
        redisOperator.del(FOODIE_SHOPCART + ":" + userId);
        return true;
    }
}
