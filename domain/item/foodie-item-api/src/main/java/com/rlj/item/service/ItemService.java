package com.rlj.item.service;

import com.rlj.item.pojo.Items;
import com.rlj.item.pojo.ItemsImg;
import com.rlj.item.pojo.ItemsParam;
import com.rlj.item.pojo.ItemsSpec;
import com.rlj.item.pojo.vo.CommentLevelCountsVO;
import com.rlj.item.pojo.vo.ShopcartVO;
import com.rlj.pojo.PagedGridResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//接口层要对外提供服务了，所以要@RequestMapping
@RequestMapping("item-api")
@FeignClient("foodie-item-service")
public interface ItemService {
    //1、根据(三级)商品ID查询商品基本信息
    @GetMapping("item")
    public Items queryItemById(@RequestParam("itemId") String itemId);
    //2、根据(三级)商品ID查询商品图片列表
    @GetMapping("itemImgs")
    public List<ItemsImg> queryItemImgList(@RequestParam("itemId")String itemId);
    //3、根据(三级)商品ID查询商品规格
    @GetMapping("itemSpecs")
    public List<ItemsSpec> queryItemSpecList(@RequestParam("itemId")String itemId);
    //4、根据(三级)商品ID查询商品参数
    @GetMapping("itemParam")
    public ItemsParam queryItemParam(@RequestParam("itemId")String itemId);
    //5、根据ID查询查询商品评价等级的数量
    @GetMapping("countComments")
    public CommentLevelCountsVO queryCommentCounts(@RequestParam("itemId")String itemId);
    //6、根据商品ID、评价等级，查询商品评价列表(需要分页的)
    @GetMapping("pageComments")
    public PagedGridResult queryPagedComments(@RequestParam("itemId")String itemId,
                                              @RequestParam(value = "level",required = false)Integer level,
                                              @RequestParam(value = "page",required = false)Integer page,
                                              @RequestParam(value = "pageSize",required = false)Integer pageSize);
    //7、根据规格ids查询最新的购物车中的商品数据(用于刷新渲染购物车中的商品数据)
    @GetMapping("getCartsBySpecIds")
    public List<ShopcartVO> queryItemsBySpecIds(@RequestParam("specIds")String specIds);
    //8、根据规格id查询对应的商品规格的信息
    @GetMapping("itemSpec")
    public ItemsSpec queryItemsSpecById(@RequestParam("specId")String specId);
    //9、根据商品id获取商品图片主图的url
    @GetMapping("primaryImage")
    public String queryItemMainImgById(@RequestParam("itemId")String itemId);
    //10、在用户提交订单之后，规格表中需要扣除库存
    @PostMapping("decreaseStock")
    public void decreaseItemSpecStock(@RequestParam("specId")String specId,@RequestParam("buyCounts")int buyCounts);
    //11、根据三级分类的商品ID搜索商品列表(需要分页的)
    @GetMapping("getItemsByThirdCat")
    public PagedGridResult searchItemsByThirdCat(@RequestParam("catId")Integer catId, @RequestParam("sort")String sort,
                                                 @RequestParam("page")Integer page, @RequestParam("pageSize")Integer pageSize);
}
