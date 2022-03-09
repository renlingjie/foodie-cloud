package com.rlj.order.fallback.itemservice;

import com.rlj.item.service.ItemCommentsService;
import com.rlj.pojo.PagedGridResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Renlingjie
 * @name  因为想实现ItemCommentsService的降级，所以需手写一个接口，直接继承ItemCommentsService，再在fallback里写即可
 * @date 2022-01-31
 */
@FeignClient(value = "foodie-item-service",fallback = ItemCommentsFallback.class)
@RequestMapping("item-comments-api")
public interface ItemCommentsFeignService extends ItemCommentsService {
}
