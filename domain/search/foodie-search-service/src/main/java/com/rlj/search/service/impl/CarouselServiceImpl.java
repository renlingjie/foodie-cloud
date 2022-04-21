package com.rlj.search.service.impl;


import com.rlj.search.mapper.CarouselMapper;
import com.rlj.search.pojo.Carousel;
import com.rlj.search.service.CarouselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;


import java.util.List;

@Service
public class CarouselServiceImpl implements CarouselService {
    @Autowired
    private CarouselMapper carouselMapper;
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<Carousel> queryAll(Integer isShow) {
        //当然可以直接调用mapper的方法查询(carouselMapper.selectAll();)，
        //这里还是创建Example后创建一个查询条件查询
        Example example = new Example(Carousel.class);
        //指定查询结果的排列顺序(按照数据库中的sort字段进行增序排列)
        example.orderBy("sort").asc();
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("isShow",isShow);
        List<Carousel> result = carouselMapper.selectByExample(example);
        return result;
    }
}
