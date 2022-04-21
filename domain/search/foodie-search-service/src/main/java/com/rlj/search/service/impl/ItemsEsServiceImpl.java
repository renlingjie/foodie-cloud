package com.rlj.search.service.impl;

import com.rlj.pojo.PagedGridResult;
import com.rlj.search.service.ItemsEsService;
import com.rlj.search.service.pojo.Items;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Renlingjie
 * @name
 * @date 2021-07-13
 */
@Service
public class ItemsEsServiceImpl implements ItemsEsService {

    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Override
    public PagedGridResult searchItems(String keywords, String sort, Integer pageIndex, Integer pageSize) {
        //确定根据什么进行排序，传入的sort：默认是"k"(商品名)，销量是"c"，价格是"p"，因此根据sort确定排序的字段
        String fieldSortName = null;
        SortOrder order = null;
        if (sort.equals("c")){
            fieldSortName = "sellCounts";
            order = SortOrder.DESC;//销量降序排列
        }else if (sort.equals("p")){
            fieldSortName = "price";
            order = SortOrder.ASC;//价格升序排列
        }else {
            fieldSortName = "itemName.keyword";//如果排序使用的字段type是文本(text)，是需要使用keyword来进行排序的
            order = SortOrder.ASC;//名称升序排列
        }
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                //定义查询内容：第一个参数表示要查询的内容，后面的参数规定从哪些"字段"(一个或多个，这里仅指定一个itemName)中间进行查询
                .withQuery(QueryBuilders.multiMatchQuery(keywords,"itemName"))
                //指定查询结果中上面的查询内容如何高亮，每个"字段"都需要设置各自的样式（拼接html标签，自定义标签样式）
                .withHighlightFields(//这里指定如果命中记录的name字段中如果有"ES"的需要填充的样式
                        new HighlightBuilder.Field("itemName").preTags("<font color='red'>").postTags("</font>"))
                //指定按照哪个"字段"进行升/降排序
                .withSort(SortBuilders.fieldSort(fieldSortName).order(order))
                //指定分页(从哪一页开始，每页多少记录)
                .withPageable(PageRequest.of(pageIndex, pageSize))
                .build();
        // 根据查询对象得到查询结果
        List<Items> itemsList = esTemplate.queryForList(searchQuery, Items.class);

        //新建一个查询数量的方法，用于创建分页所需的总记录数
        NativeSearchQuery searchQueryOfCount = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keywords,"itemName")).build();
        int totalCount = (int)esTemplate.count(searchQueryOfCount, Items.class);

        PagedGridResult gird = new PagedGridResult();
        //当前页数，(请求的第几页作为参数传进来了，这里也要返回回去)。之前为了迎合ES减1，而现在我们又回到正常的水准了，所以加1复原
        gird.setPage(pageIndex+1);//当前页数
        gird.setRows(itemsList);//每行显示的内容
        int totalPage;
        if (totalCount%pageSize==0){
            totalPage = totalCount/pageSize;
        }else {
            totalPage = totalCount/pageSize + 1;
        }
        gird.setTotal(totalPage);//总页数
        gird.setRecords(totalCount);//总记录数
        return gird;
    }

}
