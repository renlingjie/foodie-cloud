package com.rlj.item.service;

import com.rlj.pojo.PagedGridResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Renlingjie
 * @name
 * @date 2022-01-31
 */
@FeignClient("foodie-item-service")
@RequestMapping("item-comments-api")
public interface ItemCommentsService {
    //我的评价查询(分页)
    @GetMapping("myComments")
    public PagedGridResult queryMyComments(@RequestParam("userId") String userId,
                                           @RequestParam(value = "page",required = false)Integer page,
                                           @RequestParam(value = "pageSize",required = false)Integer pageSize);

    //保存评价(会被订单中心调用)
    @PostMapping("saveComments")
    public void saveComments(@RequestBody Map<String,Object> map);
}
