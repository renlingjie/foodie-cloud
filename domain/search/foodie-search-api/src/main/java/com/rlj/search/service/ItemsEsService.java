package com.rlj.search.service;
import com.rlj.pojo.PagedGridResult;

/**
 * @author Renlingjie
 * @name
 * @date 2021-07-13
 */
public interface ItemsEsService {
    public PagedGridResult searchItems(String keywords, String sort, Integer page, Integer pageSize);
}
