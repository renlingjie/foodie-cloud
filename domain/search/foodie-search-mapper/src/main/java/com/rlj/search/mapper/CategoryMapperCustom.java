package com.rlj.search.mapper;

import com.rlj.search.pojo.vo.CategoryVO;
import com.rlj.search.pojo.vo.NewItemsVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CategoryMapperCustom {
    //根据ID查询我们的商品列表
    public List<CategoryVO> getSubCatList(Integer rootCatId);
    //查询首页每个一级分类下的6条最新商品数据(参数名称由注解定义为paramsMap)
    //这里面我们当然可以定义参数Integer rootCatId，但是其实什么都是可以的，我们传一个map，获取map中的value来用
    public List<NewItemsVO> getSixNewItemsLazy(@Param("paramsMap") Map<String,Object> map);
}