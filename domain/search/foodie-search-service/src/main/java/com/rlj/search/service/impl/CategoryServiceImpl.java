package com.rlj.search.service.impl;


import com.rlj.search.mapper.CategoryMapper;
import com.rlj.search.mapper.CategoryMapperCustom;
import com.rlj.search.pojo.Category;
import com.rlj.search.pojo.vo.CategoryVO;
import com.rlj.search.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private CategoryMapperCustom categoryMapperCustom;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<Category> queryAllRootLevelCat() {
        //这里还是创建Example后创建一个查询条件查询
        Example example = new Example(Category.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("type",1);//一级分类，直接写死type==1
        List<Category> result = categoryMapper.selectByExample(example);
        return result;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<CategoryVO> getSubCatList(Integer rootCatId) {
        return categoryMapperCustom.getSubCatList(rootCatId);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List getSixNewItemsLazy(Integer rootCatId) {
        //mapper要求传入的参数是map（当然我们也可以让它传入Integer，只是这里用一下map，也算是一种学习），
        //而我们controller---->service接收的都是Integer，所以我们要new一个map
        Map<String,Object> map = new HashMap<>();
        map.put("rootCatId",rootCatId);
        return categoryMapperCustom.getSixNewItemsLazy(map);
    }
}
