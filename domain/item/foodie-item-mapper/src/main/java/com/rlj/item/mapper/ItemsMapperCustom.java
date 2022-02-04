package com.rlj.item.mapper;

import com.rlj.item.pojo.vo.ItemCommentVO;
import com.rlj.item.pojo.vo.ShopcartVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ItemsMapperCustom {
    //因为请求参数有两个(id和level)，所以我们用map封装，同时在后面的xml中，parameterType也是map
    //这里给map起了别名paramsMap，会在后面的xml中使用
    public List<ItemCommentVO> queryItemComments(@Param("paramsMap") Map<String,Object> map);

    //TODO 迁移到主搜模块foodie-search
    //因为请求参数有两个(keyWords和sort)，所以我们用map封装，同时在后面的xml中，parameterType也是map
    //这里给map起了别名paramsMap，会在后面的xml中使用
    // public List<SearchItemsVO> searchItems(@Param("paramsMap") Map<String,Object> map);
    //因为请求参数有两个(catId和sort)，所以我们用map封装，同时在后面的xml中，parameterType也是map
    //这里给map起了别名paramsMap，会在后面的xml中使用
    // public List<SearchItemsVO> searchItemsByThirdCat(@Param("paramsMap") Map<String,Object> map);

    //因为请求参数有多个(因为可能是多种规格)，且类型都相同，所以我们用list封装，同时在后面的xml中，parameterType也是list
    //这里给list起了别名paramsList，会在后面的xml中使用
    public List<ShopcartVO> queryItemsBySpecIds(@Param("paramsList") List specIdsList);
    //执行减库存操作
    public int decreaseItemSpecStock(@Param("specId") String specId,
                                     @Param("pendingCounts") int pendingCounts);
}