package com.rlj.order.fallback.itemservice;

import com.google.common.collect.Lists;
import com.rlj.item.pojo.vo.MyCommentVO;
import com.rlj.pojo.PagedGridResult;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * @author Renlingjie
 * @name
 * @date 2022-03-02
 */
@Component
@RequestMapping("XXX")  //为了避免和手写Feign接口ItemCommentsFeignService路由重复报错Ambiguous mapping的错误，直接加这么个注解，修改其路由
public class ItemCommentsFallback implements ItemCommentsFeignService{
    // 修改一下queryMyComments的降级方案--静默降级(即直接返回空)
    @Override //在这里可以使用@HystrixCommand进行多级降级啦
    public PagedGridResult queryMyComments(String userId, Integer page, Integer pageSize) {
        MyCommentVO commentVO = new MyCommentVO();
        commentVO.setContent("正在加载中");
        PagedGridResult result = new PagedGridResult();
        result.setRows(Lists.newArrayList(commentVO));
        result.setTotal(1);
        result.setRecords(1);
        return result;
    }

    @Override
    public void saveComments(Map<String, Object> map) {

    }
}
