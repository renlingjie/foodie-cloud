package com.rlj.search.controller;

import com.rlj.enums.YesOrNo;

import com.rlj.pojo.IMOOCJSONResult;
import com.rlj.search.pojo.Carousel;
import com.rlj.search.pojo.Category;
import com.rlj.search.service.CarouselService;
import com.rlj.search.service.CategoryService;
import com.rlj.search.pojo.vo.CategoryVO;
import com.rlj.search.pojo.vo.NewItemsVO;
import com.rlj.utils.JsonUtils;
import com.rlj.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Api(value = "首页",tags = {"首页展示的相关接口"})
@RequestMapping("index")
@RestController
public class IndexController {
    @Autowired
    private CarouselService carouselService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedisOperator redisOperator;
    @ApiOperation(value = "获取首页轮播图列表",notes = "获取首页轮播图列表",httpMethod = "GET")
    @GetMapping("/carousel")
    public IMOOCJSONResult carousel(){
        List<Carousel> list = new ArrayList<>();
        //首先判断缓存中是否存在轮播图的缓存，如果存在，就不需要查询数据库了，直接查询缓存即可
        String carouselStr = redisOperator.get("carousel");
        if (StringUtils.isBlank(carouselStr)){
            //传入的参数是isShow,这里我们使用枚举类(否则你来一个1/0就把它写死了)
            list = carouselService.queryAll(YesOrNo.YES.type);
            //工具类中也说明了，set、get的对象都是String，如果是其他类型，是需要转换一下的
            redisOperator.set("carousel", JsonUtils.objectToJson(list));
        }else {
            list = JsonUtils.jsonToList(carouselStr,Carousel.class);
        }

        return IMOOCJSONResult.ok(list);//IMOOCJSONResult可以通过ok返回内容，所以传入结果list
    }

    @ApiOperation(value = "获取商品分类（一级分类）",notes = "获取商品分类（一级分类）",httpMethod = "GET")
    @GetMapping("/cats")//前端的那个请求（serverUrl+'/index/cats',{}）
    public IMOOCJSONResult cats(){
        List<Category> list = new ArrayList<>();
        String catsStr = redisOperator.get("cats");
        if (StringUtils.isBlank(catsStr)){
            list = categoryService.queryAllRootLevelCat();
            redisOperator.set("cats", JsonUtils.objectToJson(list));
        }else {
            list = JsonUtils.jsonToList(catsStr,Category.class);
        }
        return IMOOCJSONResult.ok(list);//IMOOCJSONResult可以通过ok返回内容，所以传入结果list
    }

    @ApiOperation(value = "获取商品子分类",notes = "获取商品子分类",httpMethod = "GET")
    @GetMapping("/subCat/{rootCatId}")//前端的那个请求（serverUrl+'/index/subCat',{}）
    public IMOOCJSONResult subCat(@PathVariable Integer rootCatId){
        if (rootCatId == null){
            return IMOOCJSONResult.errorMsg("分类不存在");
        }
        List<CategoryVO> list = new ArrayList<>();
        String subCatsStr = redisOperator.get("subCats");
        if (StringUtils.isBlank(subCatsStr)){
            list = categoryService.getSubCatList(rootCatId);
            redisOperator.set("subCats", JsonUtils.objectToJson(list));
        }else {
            list = JsonUtils.jsonToList(subCatsStr,CategoryVO.class);
        }
        return IMOOCJSONResult.ok(list);//IMOOCJSONResult可以通过ok返回内容，所以传入结果list
    }

    @ApiOperation(value = "查询每个一级分类下的最新6条商品记录",notes = "查询每个一级分类下的最新6条商品记录",httpMethod = "GET")
    @GetMapping("/sixNewItems/{rootCatId}")//前端的那个请求（serverUrl+'/index/sixNewItems',{}）
    public IMOOCJSONResult sixNewItems(@PathVariable Integer rootCatId){
        if (rootCatId == null){
            return IMOOCJSONResult.errorMsg("分类不存在");
        }
        List<NewItemsVO> list = categoryService.getSixNewItemsLazy(rootCatId);
        return IMOOCJSONResult.ok(list);//IMOOCJSONResult可以通过ok返回内容，所以传入结果list
    }
}
