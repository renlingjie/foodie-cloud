package com.rlj.item.controller;

import com.rlj.controller.BaseController;
import com.rlj.item.pojo.Items;
import com.rlj.item.pojo.ItemsImg;
import com.rlj.item.pojo.ItemsParam;
import com.rlj.item.pojo.ItemsSpec;
import com.rlj.pojo.IMOOCJSONResult;
import com.rlj.pojo.PagedGridResult;
import com.rlj.item.pojo.vo.*;
import com.rlj.item.service.ItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "商品接口",tags = {"商品信息展示的相关接口"})
@RequestMapping("items")
@RestController
public class ItemsController extends BaseController {
    @Autowired
    private ItemService itemService;

    @ApiOperation(value = "查询商品基本信息", notes = "查询商品基本信息", httpMethod = "GET")
    @GetMapping("/info/{itemId}")//前端的那个请求（serverUrl+'/items/info',{}）
    public IMOOCJSONResult info(@PathVariable String itemId) {
        if (StringUtils.isBlank(itemId)) {
            return IMOOCJSONResult.errorMsg(null);
        }
        Items items = itemService.queryItemById(itemId);
        List<ItemsImg> itemsImgs = itemService.queryItemImgList(itemId);
        ItemsParam itemsParam = itemService.queryItemParam(itemId);
        List<ItemsSpec> itemsSpecs = itemService.queryItemSpecList(itemId);
        //IMOOCJSONResult可以通过ok返回内容，但是只能返回一个对象，所以我们又需要来一个展示层相关的VO
        ItemInfoVO itemInfoVO = new ItemInfoVO();
        itemInfoVO.setItem(items);
        itemInfoVO.setItemImgList(itemsImgs);
        itemInfoVO.setItemSpecList(itemsSpecs);
        itemInfoVO.setItemParams(itemsParam);
        return IMOOCJSONResult.ok(itemInfoVO);
    }
    @ApiOperation(value = "查询商品评价等级", notes = "查询商品评价等级", httpMethod = "GET")
    @GetMapping("/commentLevel")//前端的那个请求（serverUrl+'/items/commentLevel?itemId='+itemId,{}）
    //这里的itemId是一个请求参数，而不是像之前一样是一个路径参数，所以这里@PathVariable--->@RequestParam
    public IMOOCJSONResult commentLevel(@RequestParam String itemId) {
        if (StringUtils.isBlank(itemId)) {
            return IMOOCJSONResult.errorMsg(null);
        }
        CommentLevelCountsVO countsVO = itemService.queryCommentCounts(itemId);
        return IMOOCJSONResult.ok(countsVO);
    }
    @ApiOperation(value = "查询商品评论(分页)", notes = "查询商品评论(分页)", httpMethod = "GET")
    @GetMapping("/comments")
    //前端的那个请求（serverUrl+'/items/comments?itemId='+itemId+"&level="+level+"&page="+page+"&pageSize="+pageSize,{}）
    //这里的参数都是请求参数，而不是像之前是路径参数，所以这里@PathVariable--->@RequestParam
    public IMOOCJSONResult comments(@RequestParam String itemId,@RequestParam(required = false) Integer level,
                                    @RequestParam Integer page,@RequestParam Integer pageSize) {
        if (StringUtils.isBlank(itemId)) {
            return IMOOCJSONResult.errorMsg(null);
        }
        if (page == null) {
            page = 1;
        }
        if (pageSize == null) {
            pageSize = COMMENT_PAGE_SIZE;
        }
        PagedGridResult gird = itemService.queryPagedComments(itemId, level, page, pageSize);
        return IMOOCJSONResult.ok(gird);
    }
    //刷新购物车中的数据(主要是商品价格)
    @ApiOperation(value = "根据商品规格的ids查找最新的商品数据", notes = "根据商品规格的ids查找最新的商品数据", httpMethod = "GET")
    @GetMapping("/refresh")
    //前端的那个请求（serverUrl+'/items/refresh?itemSpecId='+itemSpecIds,{}）
    //这里的参数都是请求参数，而不是像之前是路径参数，所以这里@PathVariable--->@RequestParam
    public IMOOCJSONResult refresh(@RequestParam String itemSpecIds) {
        if (StringUtils.isBlank(itemSpecIds)) {
            return IMOOCJSONResult.ok();//你给我提供的规格是空，那么我正常返回一个空，购物车就是显示为空
        }
        List<ShopcartVO> list = itemService.queryItemsBySpecIds(itemSpecIds);
        return IMOOCJSONResult.ok(list);
    }
}
