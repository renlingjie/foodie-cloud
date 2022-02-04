package com.rlj.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rlj.enums.CommentLevel;
import com.rlj.enums.YesOrNo;
import com.rlj.item.pojo.*;
import com.rlj.item.mapper.*;
import com.rlj.pojo.PagedGridResult;
import com.rlj.item.pojo.vo.CommentLevelCountsVO;
import com.rlj.item.pojo.vo.ItemCommentVO;
import com.rlj.item.pojo.vo.ShopcartVO;
import com.rlj.item.service.ItemService;
import com.rlj.utils.DesensitizationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
//原来是@Service，Eureka是基于http的服务治理框架，故作为服务提供者，提供的服务需声明为一个Controller
@RestController
@Slf4j
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ItemsMapper itemsMapper;
    @Autowired
    private ItemsImgMapper itemsImgMapper;
    @Autowired
    private ItemsParamMapper itemsParamMapper;
    @Autowired
    private ItemsSpecMapper itemsSpecMapper;
    @Autowired
    private ItemsCommentsMapper itemsCommentsMapper;
    @Autowired
    private ItemsMapperCustom itemsMapperCustom;
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Items queryItemById(String itemId) {
        return itemsMapper.selectByPrimaryKey(itemId);
    }
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<ItemsImg> queryItemImgList(String itemId) {
        Example itemsImgExp = new Example(ItemsImg.class);
        Example.Criteria criteria = itemsImgExp.createCriteria();
        //和我们ItemsImg中的itemId(属性名)属性匹配上
        criteria.andEqualTo("itemId",itemId);
        return itemsImgMapper.selectByExample(itemsImgExp);
    }
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<ItemsSpec> queryItemSpecList(String itemId) {
        Example itemsSpecExp = new Example(ItemsSpec.class);
        Example.Criteria criteria = itemsSpecExp.createCriteria();
        //和我们ItemsSpec中的itemId(属性名)属性匹配上
        criteria.andEqualTo("itemId",itemId);
        return itemsSpecMapper.selectByExample(itemsSpecExp);
    }
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public ItemsParam queryItemParam(String itemId) {
        Example itemsParamExp = new Example(ItemsParam.class);
        Example.Criteria criteria = itemsParamExp.createCriteria();
        //和我们ItemsSpec中的itemId(属性名)属性匹配上
        criteria.andEqualTo("itemId",itemId);
        //和前面的selectByExample不同，这里只要查询一条记录即可，因为我们定义的接口返回值是一个对象而非list
        return itemsParamMapper.selectOneByExample(itemsParamExp);
    }
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public CommentLevelCountsVO queryCommentCounts(String itemId) {
        //调用我们下面的方法查询等级，同样等级这里我们还是使用一个枚举类
        Integer godCounts = getCommentCounts(itemId, CommentLevel.GOOD.type);
        Integer normalCounts = getCommentCounts(itemId, CommentLevel.NORMAL.type);
        Integer badCounts = getCommentCounts(itemId, CommentLevel.BAD.type);
        Integer totalCounts = godCounts+normalCounts+badCounts;
        CommentLevelCountsVO countsVO = new CommentLevelCountsVO();
        countsVO.setTotalCounts(totalCounts);
        countsVO.setGoodCounts(godCounts);
        countsVO.setNormalCounts(normalCounts);
        countsVO.setBadCounts(badCounts);
        return countsVO;
    }
    @Transactional(propagation = Propagation.SUPPORTS)
    Integer getCommentCounts(String itemId,Integer level){
        //这里我们就不用来Example来了，用一次我们最常用的
        ItemsComments condition = new ItemsComments();
        condition.setItemId(itemId);
        if (level != null){
            condition.setCommentLevel(level);
        }
        //这个方法我们发现查询的就是某等级的评价数，而且要求传入的是ItemsComments类型的对象
        return itemsCommentsMapper.selectCount(condition);
    }
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedGridResult queryPagedComments(String itemId, Integer level, Integer page, Integer pageSize) {
        Map<String,Object> map = new HashMap<>();
        map.put("itemId",itemId);
        map.put("level",level);
        PageHelper.startPage(page,pageSize);//查询第几页，每页多少条数据
        List<ItemCommentVO> list = itemsMapperCustom.queryItemComments(map);
        //将list中的昵称信息脱敏
        for (ItemCommentVO vo : list){
            vo.setNickName(DesensitizationUtil.commonDisplay(vo.getNickName()));
        }
        //上面的结果已经分页好啦，将其传过来，获取里面的属性，赋值封装到PagedGridResult对象中返回给前端
        return setterPagedGird(list,page);
    }
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<ShopcartVO> queryItemsBySpecIds(String specIds) {
        //将传进来的参数specIds(是前端cookie中的那个商品规格列表(xx,yy,zz))分割，得到所有的商品规格ID，存储在ids数组中
        String ids[] = specIds.split(",");
        List<String> specIdsList = new ArrayList<>();
        //调用Collections中的方法，将dis数组中的数据存储到我们的list集合的对象specIdsList中
        Collections.addAll(specIdsList,ids);
        //specIdsList中有数据后，就可以发起mapper的调用了
        return itemsMapperCustom.queryItemsBySpecIds(specIdsList);
    }
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public ItemsSpec queryItemsSpecById(String specId) {
        return itemsSpecMapper.selectByPrimaryKey(specId);
    }
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public String queryItemMainImgById(String itemId) {
        ItemsImg itemsImg = new ItemsImg();
        itemsImg.setItemId(itemId);
        itemsImg.setIsMain(YesOrNo.YES.type);
        ItemsImg result = itemsImgMapper.selectOne(itemsImg);
        return result != null ? result.getUrl() : "";
    }
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void decreaseItemSpecStock(String specId, int buyCounts) {
        //一般思路是：查询库存，判断库存是否大于购买的量，大于就执行扣库存。但是高并发下存在超买的情况
        //具体来说就是很多人同时购买，同一时刻单一请求的判断库存都是够的，但是请求扣的总和实际上是不够的
        //方法一：使用synchronized关键字，首先性能差，而且集群下就没用了
        //方法二：锁数据库，导致数据库性能低下
        //TODO 方法三：分布式锁(后面再用) zookeeper redis
        //方法四：目前暂时用乐观锁--->感觉老师说的有问题，这个加了"where 库存 > 购买数量"，是基于行锁的悲观锁
        int result = itemsMapperCustom.decreaseItemSpecStock(specId, buyCounts);
        if (result != 1){
            throw new RuntimeException("订单创建失败，原因：库存不足！");
        }
    }

    //分页方法，传入的可能是各种list，所以不写死--->List<?>
    private PagedGridResult setterPagedGird(List<?> list,Integer page){
        PageInfo<?> pageList = new PageInfo<>(list);
        PagedGridResult gird = new PagedGridResult();
        gird.setPage(page);//当前页数(请求的第几页作为参数传进来了，这里也要返回回去)
        gird.setRows(list);//总页数
        gird.setTotal(pageList.getPages());//总记录数
        gird.setRecords(pageList.getTotal());//每行显示的内容
        return gird;
    }
}
