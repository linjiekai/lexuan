package com.zhuanbo.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.JacksonUtil;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.constants.Align;
import com.zhuanbo.core.constants.CategoryLevel;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.dto.AdminShowCategoryDTO;
import com.zhuanbo.core.entity.*;
import com.zhuanbo.service.mapper.ShowCategoryMapper;
import com.zhuanbo.service.service.*;
import com.zhuanbo.service.vo.ShowCategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 展示类目表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-07-03
 */
@Service
@Slf4j
public class ShowCategoryServiceImpl extends ServiceImpl<ShowCategoryMapper, ShowCategory> implements IShowCategoryService {

    @Autowired
    private ISeqIncrService iSeqIncrService;
    @Autowired
    private ICategoryRelationService relationService;
    @Autowired
    private ICategoryService categoryService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ShowCategoryMapper showCategoryMapper;
    @Autowired
    private IShowCategoryGoodsService iShowCategoryGoodsService;

    //特殊id
    private static final long[] ispecialIds = {0L, 400001L};

    @Override
    public void setChildrenCache(Long id) throws Exception {
        List<ShowCategory> childrenList = new ArrayList<>();

        Map<String, Object> params = null;
        //顶级父类 pid=0
        if (id == 0L) {
            params = new HashMap<String, Object>();
            params.put("pid", id);
            params.put("status", 1);
            //查库获取所有子节点
            childrenList = this.getChildren(params);
            if (childrenList != null && childrenList.size() > 0) {
                for (ShowCategory showCategory : childrenList) {
                    showCategory.setChildrenList(new ArrayList<ShowCategory>());
                }
                //这里缓存的是0级+1级类目树
                RedisUtil.set(ConstantsEnum.SHOW_CATEGORY_CHILDREN.stringValue() + id, childrenList, Constants.CACHE_EXP_TIME);
            }
            return;
        }

        ShowCategory showCategory = this.getById(id);
        //3级没有子类目，直接返回
        if (showCategory.getLevel() == CategoryLevel.LEVEL3.getId()) {
            return;
        }
        //如果传进来是1级类目
        if (RedisUtil.get(ConstantsEnum.SHOW_CATEGORY_CHILDREN.stringValue() + 0) == null
                || showCategory.getLevel().intValue() == CategoryLevel.LEVEL1.getId()) {

            params = new HashMap<String, Object>();
            params.put("pid", 0L);
            params.put("status", 1);

            childrenList = this.getChildren(params);
            if (childrenList != null && childrenList.size() > 0) {
                for (ShowCategory obj : childrenList) {
                    obj.setChildrenList(new ArrayList<ShowCategory>());
                }
                //这里缓存的是0级+1级类目树
                RedisUtil.set(ConstantsEnum.SHOW_CATEGORY_CHILDREN.stringValue() + 0, childrenList, Constants.CACHE_EXP_TIME);
            }
        }

        params = new HashMap<String, Object>();
        params.put("pid", id);
        params.put("status", 1);
        childrenList = new ArrayList<>();
        //获取所有子节点
        getChildrenAll(childrenList, params);
        if (childrenList != null && childrenList.size() > 0) {

            //缓存子节点自身
            for (ShowCategory obj : childrenList) {
                //如果id是一级节点，此处为二级节点，如果id是二级节点，此处是三级节点
                RedisUtil.set(ConstantsEnum.SHOW_CATEGORY_ENTITY.stringValue() + obj.getId(), obj, Constants.CACHE_EXP_TIME);
                if (null != obj.getChildrenList() && obj.getChildrenList().size() > 0) {
                    //缓存子节点自身,此处为三级节点
                    for (ShowCategory chObj : obj.getChildrenList()) {
                        RedisUtil.set(ConstantsEnum.SHOW_CATEGORY_ENTITY.stringValue() + chObj.getId(), chObj, Constants.CACHE_EXP_TIME);
                    }
                }
            }

            //这里存的是2级+3级类目树/1+3级类目树
            RedisUtil.set(ConstantsEnum.SHOW_CATEGORY_CHILDREN.stringValue() + id, childrenList, Constants.CACHE_EXP_TIME);
        }

    }

    @Override
    public void setShowCategoryCache(Long id) {
        ShowCategory showCategory = this.getById(id);
        //三级的关系表
        if (null != showCategory && showCategory.getLevel().intValue() == CategoryLevel.LEVEL3.getId()) {
            List<CategoryRelation> categoryRelationList = relationService.list(new QueryWrapper<CategoryRelation>().eq("show_id", showCategory.getId()));
            showCategory.setCategoryRelationList(categoryRelationList);
        }

        RedisUtil.set(ConstantsEnum.SHOW_CATEGORY_ENTITY.stringValue() + id, showCategory, Constants.CACHE_EXP_TIME);
    }


    @Override
    public List<ShowCategory> getChildren(Map<String, Object> params) throws Exception {
        List<ShowCategory> data = baseMapper.getChildren(params);
        if (null == data) {
            return new ArrayList<>();
        }
        return data;
    }

    @Override
    public List<ShowCategory> getChildrenCache(Long id) throws Exception {
        List<ShowCategory> childrenList = (List<ShowCategory>) RedisUtil.get(ConstantsEnum.SHOW_CATEGORY_CHILDREN.stringValue() + id);

        if (null == childrenList || childrenList.size() <= 0) {
            return new ArrayList<>();
        }

        return childrenList;
    }

    @Override
    public void getChildrenAll(List<ShowCategory> data, Map<String, Object> params) throws Exception {

        List<ShowCategory> childrenList = this.getChildren(params);
        if (null == childrenList || childrenList.size() <= 0) {
            return;
        }
        data.addAll(childrenList);
        List<ShowCategory> childrenListTemp = null;
        List<CategoryRelation> categoryRelationList = null;
        for (ShowCategory obj : childrenList) {
            if (CategoryLevel.LEVEL3.getId() == obj.getLevel().intValue()) {
                categoryRelationList = relationService.list(new QueryWrapper<CategoryRelation>().eq("show_id", obj.getId()));
                obj.setCategoryRelationList(categoryRelationList);
                obj.setChildrenList(new ArrayList<ShowCategory>());
            }

            Map<String, Object> tempMap = new HashMap<String, Object>();
            tempMap.putAll(params);
            tempMap.put("pid", obj.getId());
            childrenListTemp = new ArrayList<ShowCategory>();
            //递归调用
            getChildrenAll(childrenListTemp, tempMap);
            obj.setChildrenList(childrenListTemp);
        }
    }

    @Override
    public void getChildrenAllCache(List<ShowCategory> data, Long id) throws Exception {
        List<ShowCategory> childrenList = this.getChildrenCache(id);

        if (null == childrenList || childrenList.size() <= 0) {
            return;
        }

        data.addAll(childrenList);

        List<ShowCategory> childrenListTemp = null;
        for (ShowCategory obj : childrenList) {

            if (CategoryLevel.LEVEL3.getId() == obj.getLevel().intValue()) {
                return;
            }
            childrenListTemp = new ArrayList<>();
            getChildrenAllCache(childrenListTemp, obj.getId());
            obj.setChildrenList(childrenListTemp);
        }
    }

    @Override
    public ShowCategory getParentRoot(Long id) {

        ShowCategory showCategory = this.getById(id);
        if (null == showCategory) {
            return null;
        }

        if (showCategory.getLevel().intValue() != CategoryLevel.LEVEL1.getId()) {
            showCategory = getParentRoot(showCategory.getPid());
        }

        return showCategory;
    }

    @Override
    public ShowCategory getParentRootCache(Long id) {

        ShowCategory showCategory = (ShowCategory) RedisUtil.get(ConstantsEnum.SHOW_CATEGORY_ENTITY.stringValue() + id);
        if (null == showCategory) {
            return null;
        }

        if (showCategory.getLevel().intValue() != CategoryLevel.LEVEL1.getId() && 0L != showCategory.getPid()) {
            showCategory = getParentRootCache(showCategory.getPid());
        }

        return showCategory;
    }

    @Override
    public ShowCategory getShowCategoryCache(Long id) {
        return (ShowCategory) RedisUtil.get(ConstantsEnum.SHOW_CATEGORY_ENTITY.stringValue() + id);
    }

    @Override
    public void deleteCache(Long id) throws Exception {
        ShowCategory showCategory = this.getById(id);

        if (showCategory == null) {
            return;
        }

        //如果删除的分类是一级分类，则需要刷新pid等于0的分类，并且把自身从顶级分类树中移除
        if (showCategory.getPid() == 0) {
            this.setChildrenCache(0L);
        } else {
            //获取一级类目
            ShowCategory parent = this.getParentRoot(id);
            this.setChildrenCache(parent.getId());
        }

        //缓存一律直接删
        RedisUtil.del(ConstantsEnum.SHOW_CATEGORY_ENTITY.stringValue() + id);
        RedisUtil.del(ConstantsEnum.SHOW_CATEGORY_CHILDREN.stringValue() + id);

        //如果不是三级分类，删除所有子节点
        if (showCategory.getLevel() != CategoryLevel.LEVEL3.getId()) {
            List<ShowCategory> data = this.list(new QueryWrapper<ShowCategory>().eq("pid", id));
            List<ShowCategory> dataChildren = null;
            for (ShowCategory obj : data) {
                //如果id是一级节点，此处为二级节点，如果id是二级节点，此处是三级节点
                RedisUtil.del(ConstantsEnum.SHOW_CATEGORY_ENTITY.stringValue() + obj.getId());

                if (obj.getLevel() == CategoryLevel.LEVEL2.getId()) {
                    dataChildren = this.list(new QueryWrapper<ShowCategory>().eq("pid", obj.getId()));
                    //删除子节点自身,此处为三级节点
                    for (ShowCategory chObj : dataChildren) {
                        RedisUtil.del(ConstantsEnum.SHOW_CATEGORY_ENTITY.stringValue() + chObj.getId());
                    }
                }

            }
        }

    }

    public void refreshCache(Long id) throws Exception {
        ShowCategory showCategory = this.getById(id);
        //如果状态是下线，则删除缓存
        if (showCategory.getStatus() != ConstantsEnum.SHOW_CATEGORY_STATUS_1.integerValue()
                || showCategory.getDeleted() == 1) {
            deleteCache(id);
            return;
        }
        //缓存自身
        setShowCategoryCache(id);

        //获取一级类目
        ShowCategory parent = this.getParentRoot(id);
        if (parent != null) {
            //如果状态是下线，则删除缓存
            if (parent.getStatus() != ConstantsEnum.SHOW_CATEGORY_STATUS_1.integerValue()
                    || parent.getDeleted() == 1) {
                deleteCache(id);
                return;
            }
            setChildrenCache(parent.getId());
        }
    }

    @Override
    public void refreshAllCache() throws Exception {

        List<ShowCategory> childrenList = this.list(new QueryWrapper<ShowCategory>().eq("status", 1).eq("deleted", 0).eq("level", "1").orderByDesc("indexs"));

        for (ShowCategory showCategory : childrenList) {
            setShowCategoryCache(showCategory.getId());
            setChildrenCache(showCategory.getId());
        }
        //首页展示分类缓存
        refreshIndexShowCategoryCache();

    }

    @Override
    public Object queryList(AdminShowCategoryDTO dto) throws Exception {
        Page<ShowCategory> pageCond = new Page<>(dto.getPage(), dto.getLimit());
        QueryWrapper<ShowCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);
        //id 和名称模糊查询
        Optional.ofNullable(dto.getId()).ifPresent(n -> queryWrapper.eq("id", n));
        Optional.ofNullable(dto.getName()).ifPresent(n -> queryWrapper.like("name", n));
        Optional.ofNullable(dto.getLevel()).ifPresent(n -> queryWrapper.eq("level", n));
        Optional.ofNullable(dto.getPid()).ifPresent(n -> queryWrapper.eq("pid", n));
        queryWrapper.orderByDesc("indexs");
        IPage<ShowCategory> adIPage = this.page(pageCond, queryWrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("total", adIPage.getTotal());
        data.put("items", adIPage.getRecords());
        return ResponseUtil.ok(data);
    }

    @Override
    public Object deleteShowCategory(Long id) throws Exception {
        //过滤特殊id
        Arrays.stream(ispecialIds).forEach(s -> {
            if (s == id.longValue()) {
                throw new ShopException(71009);
            }
        });
        Set<Long> ids = new LinkedHashSet<>();
        Set<Long> idLevel3 = new LinkedHashSet<>();
        List<ShowCategory> data = new LinkedList<>();
        Map<String, Object> params = new HashMap<String, Object>();
        try {
            params.put("pid", id);
            ShowCategory sc = getById(id);
            Optional.ofNullable(sc).orElseThrow(() -> new ShopException(10404));
            switch (sc.getLevel().intValue()) {
                case 1:
                    getChildrenAll(data, params);
                    break;
                case 2:
                    getChildrenAll(data, params);
                    break;
                case 3:
                    idLevel3.add(id);
                    break;
            }
            //遍历整棵树
            if (!CollectionUtils.isEmpty(data)) {
                for (ShowCategory s : data) {
                    ids.add(s.getId());
                    if (CategoryLevel.LEVEL3.getId() == s.getLevel().intValue()) {
                        idLevel3.add(s.getId());
                    }
                    for (ShowCategory c : s.getChildrenList()) {
                        ids.add(c.getId());
                        if (CategoryLevel.LEVEL3.getId() == c.getLevel().intValue()) {
                            idLevel3.add(c.getId());
                        }
                    }
                }
            }

            //删库
            ids.add(id);
            //状态改成下线，并且删除
            update(new ShowCategory(), new UpdateWrapper<ShowCategory>().set("indexs", -1).set("status", 2).set("DELETED", 1).in("id", ids));
            //删除关联表
            if (!CollectionUtils.isEmpty(idLevel3)) {
                relationService.remove(new QueryWrapper<CategoryRelation>().in("show_id", idLevel3));
            }
            //删除缓存
            deleteCache(id);
        } catch (Exception ex) {
            log.error("{} deleteShowCategory error", id, ex);
            if (ex instanceof ShopException || ex.getCause() instanceof ShopException) {
                return ResponseUtil.result(Integer.valueOf(((ShopException) ex).getCode()));
            }
            return ResponseUtil.serious();
        }
        return ResponseUtil.ok();
    }

    @Override
    public Object creatShowCategory(AdminShowCategoryDTO dto) throws Exception {
        ShowCategoryVO vo = new ShowCategoryVO();
        ShowCategory showCategory = new ShowCategory();
        List<Goods> goods = Lists.newArrayList();
        //校验下数据
        dataValidate(dto, vo, 0);
        try {
            BeanUtils.copyProperties(dto, showCategory);
            //先插入展示分类
            showCategory.setId(Long.valueOf(getCategoryId(showCategory.getLevel())));
            ShowCategory parent = this.getById(showCategory.getPid());
            if (parent != null) {
                showCategory.setEnable(parent.getEnable());
            }
            boolean save = this.save(showCategory);
            if (save) {
                switch (showCategory.getLevel().intValue()) {
                    //三级分类
                    case 3:
                        batchSaveCategoryRelation(dto, showCategory.getId());
                        break;
                    //一级分类
                    case 1:
                        batchSaveShowCategoryGoods(dto, showCategory.getId(), goods);
                        break;
                }
            }

        } catch (Exception e) {
            log.error("{} deleteShowCategory error{}", JacksonUtil.objTojson(dto), e);
            if (e instanceof ShopException || e.getCause() instanceof ShopException) {
                return ResponseUtil.result(Integer.valueOf(((ShopException) e).getCode()));
            }
            return ResponseUtil.serious();
        }
        BeanUtils.copyProperties(showCategory, vo);
        vo.setUpdateTime(vo.getAddTime());
        vo.setGoods(goods);
        //刷新缓存
        refreshCache(vo.getId());
        return ResponseUtil.ok(vo);
    }


    /**
     * @Description(描述): 批量插入关系表 shop_category_relation
     * @auther: Jack Lin
     * @date: 2019/7/5 11:30
     */
    private void batchSaveCategoryRelation(AdminShowCategoryDTO dto, Long id) throws Exception {
        Set<Long> idSet = new LinkedHashSet<>();
        try {
            //先删除关联
            Optional.ofNullable(id).ifPresent(s -> {
                relationService.remove(new QueryWrapper<CategoryRelation>().eq("show_id", id));
            });
            //存放要插入的关系对象
            List<CategoryRelation> list = new LinkedList<>();
            List<Long> cateids = null;
            if (dto.getCateIds() != null && dto.getCateIds().length > 0) {
                cateids = Arrays.asList(dto.getCateIds());
                List<Category> categories = categoryService.selectBatchIds(cateids);
                //过滤：关联的商品分类是否存在，并且是否属于3级分类
                Iterator<Category> iterator = categories.iterator();
                while (iterator.hasNext()) {
                    Category category = iterator.next();
                    if (CategoryLevel.LEVEL3.getId() != category.getLevel().intValue()) {
                        //把不属于3级分类的记录
                        idSet.add(category.getId());
                        iterator.remove();
                    }
                }
                categories.stream().forEach(s -> {
                    CategoryRelation categoryRelation = new CategoryRelation();
                    categoryRelation.setCategoryId(s.getId());
                    categoryRelation.setShowId(id);
                    list.add(categoryRelation);
                });
            }
            if (dto.getGoodsIds() != null && dto.getGoodsIds().length > 0) {
                List<Long> goodsids = Arrays.asList(dto.getGoodsIds());
                List<Goods> goods = goodsService.selectBatchIds(goodsids);
                goods.stream().forEach(s -> {
                    CategoryRelation categoryRelation = new CategoryRelation();
                    categoryRelation.setGoodsId(s.getId().longValue());
                    categoryRelation.setShowId(id);
                    list.add(categoryRelation);
                });
            }
            if (!CollectionUtils.isEmpty(idSet)) {
                log.info("无效关联类目：{}", JSON.toJSONString(idSet));
            }

            if (!CollectionUtils.isEmpty(list)) {
                boolean b = relationService.saveBatch(list);
                log.info("批量插入 CategoryRelation ：{},条数：{}", b, list.size());
            }
        } catch (Exception e) {
            log.error("{}:批量插入 CategoryRelation error:{}", JacksonUtil.objTojson(dto), e);
            throw e;
        }
    }

    @Override
    public Object updateShowCategory(AdminShowCategoryDTO dto) throws Exception {
        ShowCategoryVO vo = new ShowCategoryVO();
        ShowCategory old = getById(dto.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ShopException(10404));
        dataValidate(dto, vo, 1);
        ShowCategory showCategory = new ShowCategory();
        List<Goods> goods = Lists.newArrayList();
        try {
            dto.setEnable(dto.getEnable() == null ? 1 : dto.getEnable());
            BeanUtils.copyProperties(dto, showCategory);
            boolean b = this.updateById(showCategory);
            if (b) {
                switch (showCategory.getLevel().intValue()) {
                    //三级分类
                    case 3:
                        batchSaveCategoryRelation(dto, showCategory.getId());
                        break;
                    //一级分类,至少添加一款商品
                    case 1:
                        batchSaveShowCategoryGoods(dto, showCategory.getId(), goods);
                        break;
                }
            }
            BeanUtils.copyProperties(showCategory, vo);
            if (CategoryLevel.LEVEL3.getId() != showCategory.getLevel().intValue()) {
                List<Long> longs = this.baseMapper.selectChildrenIds(showCategory.getId());
                for (Iterator<Long> i = longs.iterator(); i.hasNext(); ) {
                    if (i.next().longValue() == showCategory.getId().longValue()) {
                        i.remove();
                    }
                }
                //父类目改为展示，子类目也都要改为展示
                if (!CollectionUtils.isEmpty(longs)) {
                    update(new ShowCategory(), new UpdateWrapper<ShowCategory>().set("enable", showCategory.getEnable()).in("id", longs));
                }
            }

            //刷新缓存
            refreshCache(vo.getId());
            //更新前后父类目不一致，需要更新新旧父类目缓存
            if (old.getPid().longValue() != vo.getPid().longValue()) {
                //删除旧父节点缓存
                RedisUtil.del(ConstantsEnum.SHOW_CATEGORY_CHILDREN.stringValue() + old.getPid());
                //重新设置下缓存
                refreshCache(old.getPid());
            }
        } catch (Exception e) {
            log.error("{} updateShowCategory error{}", JacksonUtil.objTojson(dto), e);
            if (e instanceof ShopException || e.getCause() instanceof ShopException) {
                return ResponseUtil.result(Integer.valueOf(((ShopException) e).getCode()));
            }
            return ResponseUtil.serious();
        }
        vo.setGoods(goods);
        return ResponseUtil.ok(vo);

    }

    @Override
    public Object detail(Long id) throws Exception {
        ShowCategory showCategory = getById(id);
        Optional.ofNullable(showCategory).orElseThrow(() -> new ShopException(10404));
        List<CategoryRelation> list = relationService.list(new QueryWrapper<CategoryRelation>().eq("show_id", id));
        if (!CollectionUtils.isEmpty(list)) {
            showCategory.setCategoryRelationList(list);
        }
        //父节点
        ShowCategory parent = getById(showCategory.getPid());
        ShowCategoryVO vo = new ShowCategoryVO();
        BeanUtils.copyProperties(showCategory, vo);
        vo.setPidName(parent != null ? parent.getName() : "");
        List<Goods> goodsList = Lists.newArrayList();
        //一级类目
        if (CategoryLevel.LEVEL1.getId() == showCategory.getLevel().intValue()) {
            List<ShowCategoryGoods> showCategoryGoods = iShowCategoryGoodsService.list(new QueryWrapper<ShowCategoryGoods>().in("show_category_id", id));
            if (!CollectionUtils.isEmpty(showCategoryGoods)) {
                List<Long> longs = showCategoryGoods.stream().map(s -> s.getGoodsId()).collect(Collectors.toList());
                goodsList = goodsService.list(new QueryWrapper<Goods>().eq("deleted", 0).in("id", longs));
                vo.setGoods(goodsList);
            }
        }
        vo.setGoods(goodsList);
        return ResponseUtil.ok(vo);
    }


    /**
     * @param :[dto, vo, type]  type 0创建，1更新
     * @return :void
     * @Description(描述):
     * @auther: Jack Lin
     * @date: 2019/7/15 11:07
     */
    private void dataValidate(AdminShowCategoryDTO dto, ShowCategoryVO vo, Integer type) throws ShopException, Exception {
        int level = dto.getLevel().intValue();
        List<ShowCategory> list = list(new QueryWrapper<ShowCategory>().eq("pid", dto.getPid()).eq("deleted", ConstantsEnum.DELETED_0.stringValue()).eq("indexs", dto.getIndexs()).eq("level", level));
        if (!CollectionUtils.isEmpty(list)) {
            //如果是创建直接抛错
            if (0 == type) {
                throw new ShopException(71007);
            }
            for (Iterator<ShowCategory> i = list.iterator(); i.hasNext(); ) {
                ShowCategory sh = i.next();
                if (dto.getId().longValue() == sh.getId().longValue()) {
                    continue;
                }
                //更新的话除了本身还有其他则抛错
                throw new ShopException(71007);
            }
        }
        if (CategoryLevel.LEVEL1.getId() == level) {
            dto.setPid(0l);
            return;
        }
        Optional.ofNullable(dto).ifPresent(c -> {
            ShowCategory parent = getById(dto.getPid());
            //还是为空就抛错
            Optional.ofNullable(parent).orElseThrow(() -> new ShopException(71003));
            int level1 = parent.getLevel().intValue();
            switch (level) {
                case 2:
                    Optional.ofNullable(level).filter(l -> CategoryLevel.LEVEL1.getId() == level1).orElseThrow(() -> new ShopException(71005));
                    break;
                case 3:
                    if ((dto.getCateIds() == null && dto.getGoodsIds() == null) || (dto.getCateIds().length <= 0 && dto.getGoodsIds().length <= 0)) {
                        throw new ShopException(71002);
                    }
                    Optional.ofNullable(level).filter(l -> CategoryLevel.LEVEL2.getId() == level1).orElseThrow(() -> new ShopException(71006));
                    break;
            }
            //返回结果带上父类目名称
            vo.setPidName(parent.getName());
        });
    }

    /**
     * @param :[dto, id]
     * @return :void
     * @Description(描述): 一级展示分类批量插入另一张关系表 shop_show_category_goods
     * @auther: Jack Lin
     * @date: 2019/8/12 19:57
     */
    private void batchSaveShowCategoryGoods(AdminShowCategoryDTO dto, Long id, List<Goods> goods) {
        //先删除
        iShowCategoryGoodsService.remove(new QueryWrapper<ShowCategoryGoods>().eq("show_category_id", id));
        List<Long> longs = Arrays.asList(dto.getGoodsIds());
        List<Goods> goodsList = goodsService.selectBatchIds(longs);
        //先过滤无效商品
        for (Iterator<Goods> i = goodsList.iterator(); i.hasNext(); ) {
            Goods good = i.next();
            if (ConstantsEnum.DELETED_1.integerValue().intValue() == good.getStatus().intValue()) {
                i.remove();
                continue;
            }
            Goods g = new Goods();
            g.setId(good.getId());
            g.setStatus(good.getStatus());
            goods.add(g);
        }
        List<Long> list = goodsList.stream().map(s -> Long.valueOf(s.getId())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(list)) {
            List<ShowCategoryGoods> shs = new LinkedList<>();
            list.stream().forEach(ss -> {
                ShowCategoryGoods sh = new ShowCategoryGoods();
                sh.setShowCategoryId(id);
                sh.setGoodsId(ss);
                shs.add(sh);

            });
            //批量插入
            iShowCategoryGoodsService.saveBatch(shs);
        }
    }

    /**
     * @param :[level]
     * @return :java.lang.String
     * @Description(描述): 获取id
     * @auther: Jack Lin
     * @date: 2019/7/23 17:02
     */
    public String getCategoryId(int level) {
        switch (level) {
            case 1:
                return iSeqIncrService.nextVal("show_category_leve1_id", 6, Align.LEFT);
            case 2:
                return iSeqIncrService.nextVal("show_category_leve2_id", 6, Align.LEFT);
            case 3:
                return iSeqIncrService.nextVal("show_category_leve3_id", 6, Align.LEFT);
        }
        return null;
    }


    @Override
    public void refreshIndexShowCategoryCache() throws Exception {
        List<ShowCategoryVO> vos = Lists.newArrayList();
        List<ShowCategory> showCategories = this.list(new QueryWrapper<ShowCategory>().eq("level", CategoryLevel.LEVEL1.getId()).eq("deleted", ConstantsEnum.DELETED_0.stringValue()));
        List<Long> showIds = showCategories.stream().map(s -> s.getId()).collect(Collectors.toList());
        List<ShowCategoryGoods> showCategoryGoods = iShowCategoryGoodsService.list(new QueryWrapper<ShowCategoryGoods>().in("show_category_id", showIds));
        List<Long> goodIds = showCategoryGoods.stream().map(s -> s.getGoodsId()).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(goodIds)) {
            List<Goods> goodsList = goodsService.selectBatchIds(goodIds);
            //不想遍历查数据库，改为一次查出数据，再遍历三个集合
            showCategories.stream().forEach(s -> {
                ShowCategoryVO vo = new ShowCategoryVO();
                vo.setId(s.getId());
                vo.setShowIndexUrl(s.getShowIndexUrl());
                List<Goods> goods = Lists.newArrayList();
                showCategoryGoods.stream().forEach(sh -> {
                    if (sh.getShowCategoryId().longValue() == s.getId().longValue()) {
                        goodsList.stream().forEach(g -> {
                            if (sh.getGoodsId().longValue() == g.getId().intValue()) {
                                Goods good = new Goods();
                                good.setId(g.getId());
                                good.setName(g.getName());
                                good.setCoverImages(g.getCoverImages());
                                goods.add(good);
                            }
                        });
                    }
                });
                vo.setGoods(goods);
                if (!CollectionUtils.isEmpty(goods)) {
                    vos.add(vo);
                }
            });
        }
        RedisUtil.set(ConstantsEnum.REDIS_INDEX_SHOWCATEGORY.stringValue(), vos, Constants.CACHE_EXP_TIME);

    }
}
