package com.rlj.item.mapper;

import com.rlj.my.mapper.MyMapper;
import com.rlj.item.pojo.ItemsComments;
import com.rlj.item.pojo.vo.MyCommentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ItemsCommentsMapperCustom extends MyMapper<ItemsComments> {
    //保存评价，传入的参数有两个，一个userId，一个commentList
    public void saveComments(Map<String, Object> map);
    //查询我的所有历史评价
    public List<MyCommentVO> queryMyComments(@Param("paramsMap") Map<String, Object> map);

}