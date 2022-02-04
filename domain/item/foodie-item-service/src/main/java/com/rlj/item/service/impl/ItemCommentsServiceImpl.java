package com.rlj.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rlj.item.mapper.ItemsCommentsMapperCustom;
import com.rlj.item.pojo.vo.MyCommentVO;
import com.rlj.item.service.ItemCommentsService;
import com.rlj.pojo.PagedGridResult;
import com.rlj.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Renlingjie
 * @name
 * @date 2022-01-31
 */
@RestController
public class ItemCommentsServiceImpl extends BaseService implements ItemCommentsService {
    @Autowired
    private ItemsCommentsMapperCustom itemsCommentsMapperCustom;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedGridResult queryMyComments(String userId, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        PageHelper.startPage(page, pageSize);
        List<MyCommentVO> list = itemsCommentsMapperCustom.queryMyComments(map);
        return setterPagedGird(list, page);
    }

    @Override
    public void saveComments(Map<String, Object> map) {
        itemsCommentsMapperCustom.saveComments(map);
    }

    //分页方法，传入的可能是各种list，所以不写死--->List<?>
    private PagedGridResult setterPagedGird(List<?> list,Integer page){
        PageInfo<?> pageList = new PageInfo<>(list);
        PagedGridResult gird = new PagedGridResult();
        gird.setPage(page);//当前页数(请求的第几页作为参数传进来了，这里也要返回回去)
        gird.setRows(list);//每行显示的内容
        gird.setTotal(pageList.getPages());//总页数
        gird.setRecords(pageList.getTotal());//总记录数
        return gird;
    }
}
