package com.rlj.search.controller;

import com.rlj.pojo.IMOOCJSONResult;
import com.rlj.pojo.PagedGridResult;
import com.rlj.search.service.ItemsEsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//该注解让返回的所有请求都是json对象
@RestController
@RequestMapping("items")
public class ItemsController {

    @Autowired
    private ItemsEsService itemsEsService;

    @GetMapping("/search")
    public IMOOCJSONResult search(String keywords, String sort,
                                  Integer page, Integer pageSize) {
        if (StringUtils.isBlank(keywords)) {
            return IMOOCJSONResult.errorMsg(null);
        }
        if (page == null) {
            page = 0;//ES的分页默认是从0开始的
        }else {
            page--;//ES的分页默认是从0开始的，我们查询第一页发送的请求就是1，其实是要为0，所以减1
        }
        if (pageSize == null) {
            pageSize = 10;//在这里设置死每页查询10条记录
        }
        PagedGridResult gird = itemsEsService.searchItems(keywords, sort, page, pageSize);
        return IMOOCJSONResult.ok(gird);
    }

}
