package com.zhuanbo.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.constants.Align;
import com.zhuanbo.core.constants.CategoryLevel;
import com.zhuanbo.core.dto.AdminCategoryDTO;
import com.zhuanbo.core.entity.Category;
import com.zhuanbo.core.entity.Goods;
import com.zhuanbo.service.mapper.CategoryMapper;
import com.zhuanbo.service.service.ICategoryService;
import com.zhuanbo.service.service.IGoodsService;
import com.zhuanbo.service.service.ISeqIncrService;
import com.zhuanbo.service.vo.CategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 商品类目表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-07-03
 */
@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    @Autowired
    private ISeqIncrService iSeqIncrService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private RedissonDistributedLocker redissonLocker;
    private static final String Lock_update_Category = "lock_update_category_";

    @Override
    public Object queryList(AdminCategoryDTO dto) {
        Page<Category> pageCond = new Page<>(dto.getPage(), dto.getLimit());
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);
        //id 和名称模糊查询
        Optional.ofNullable(dto.getId()).ifPresent(n -> queryWrapper.eq("id", n));
        Optional.ofNullable(dto.getName()).ifPresent(n -> queryWrapper.like("name", n));
        Optional.ofNullable(dto.getLevel()).ifPresent(n -> queryWrapper.eq("level", n));
        Optional.ofNullable(dto.getPid()).ifPresent(n -> queryWrapper.eq("pid", n));

        IPage<Category> adIPage = this.page(pageCond, queryWrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("total", adIPage.getTotal());
        List<Category> records = adIPage.getRecords();
        List<CategoryVO> categoryVOS = new ArrayList<>();
        records.stream().forEach(s -> {
            CategoryVO vo = new CategoryVO();
            BeanUtils.copyProperties(s, vo);
            categoryVOS.add(vo);
        });
        data.put("items", records);
        return ResponseUtil.ok(data);
    }

    @Override
    public Object deleteCategory(Long id) {
        CategoryVO vo = selectOne(id);
        if (vo == null) {
            return ResponseUtil.ok();
        }
        //类目下有商品，不允许删除
        List<Long> ids = new LinkedList<>();
        int count = 0;
        switch (vo.getLevel()) {
            //三级类目
            case 3:
                count = goodsService.count(new QueryWrapper<Goods>().eq("category_id", id));
                break;
            //一级二级类目需要查询所有叶子节点
            default:
                ids = categoryMapper.queryCateIds(id);
                count = goodsService.count(new QueryWrapper<Goods>().in("category_id", ids));
        }
        if (count != 0) {
            return ResponseUtil.result(71001);
        }
        try {
            //逻辑删除
            if (!CollectionUtils.isEmpty(ids)) {
                this.baseMapper.update(new Category(), new UpdateWrapper<Category>().set("deleted", 1).in("id", ids));
            }
        } catch (Exception ex) {
            log.error("{} deleteCategory error:{}", id, ex);
            return ResponseUtil.serious();
        }

        return ResponseUtil.ok();
    }


    @Override
    public Object creatCategory(AdminCategoryDTO dto) {
        CategoryVO vo = null;
        try {
            vo = new CategoryVO();
            //校验下数据
            dataValidate(dto, vo);
            Category category = new Category();
            BeanUtils.copyProperties(dto, category);
            //获取id
            category.setId(Long.valueOf(getCategoryId(category.getLevel())));
            this.save(category);
            BeanUtils.copyProperties(category, vo);
            vo.setUpdateTime(vo.getAddTime());
        } catch (Exception ex) {
            log.error("{} : creatCategory error", JSON.toJSONString(dto), ex);
            if (ex instanceof ShopException || ex.getCause() instanceof ShopException) {
                return ResponseUtil.result(Integer.valueOf(((ShopException) ex).getCode()));
            }
            return ResponseUtil.serious();
        }
        return ResponseUtil.ok(vo);
    }

    @Override
    public Object updateCategory(AdminCategoryDTO dto) {
        CategoryVO vo = null;
        String lockKey = Lock_update_Category + dto.getId();
        boolean lock = redissonLocker.tryLock(lockKey, TimeUnit.SECONDS, 10, 30);
        if (!lock) {
            return ResponseUtil.result(22001);
        }
        try {
            vo = new CategoryVO();
            dataValidate(dto, vo);
            Category category = new Category();
            BeanUtils.copyProperties(dto, category);
            this.updateById(category);
            BeanUtils.copyProperties(category, vo);
            return ResponseUtil.ok(vo);
        } catch (Exception ex) {
            log.error("{} : updateCategory error", JSON.toJSONString(dto), ex);
            if (ex instanceof ShopException || ex.getCause() instanceof ShopException) {
                return ResponseUtil.result(Integer.valueOf(((ShopException) ex).getCode()));
            }
            return ResponseUtil.serious();
        } finally {
            if(lock){
                redissonLocker.unlock(lockKey);
            }
        }

    }

    @Override
    public List<Category> selectBatchIds(List<Long> ids) {
        return categoryMapper.selectBatchIds(ids);
    }

    @Override
    public Object detail(Long id) {
        CategoryVO vo = selectOne(id);
        if (vo == null) {
            return ResponseUtil.badResult();
        } else {
            return ResponseUtil.ok(vo);
        }
    }

    @Override
    public CategoryVO selectOne(Long id) {
        try {
            CategoryVO vo = new CategoryVO();
            Category category = this.getById(id);
            BeanUtils.copyProperties(category, vo);
            Category parent = this.getById(vo.getPid());
            if (parent != null) {
                vo.setPidName(parent.getName());
            }
            return vo;
        } catch (Exception e) {
            log.error("id= {} selectOne error:{}", id, e);
            return null;
        }

    }

    private void dataValidate(AdminCategoryDTO dto, CategoryVO vo) throws ShopException {
        int level = dto.getLevel().intValue();
        //一级类目不用校验level和父目录
        if (CategoryLevel.LEVEL1.getId() == level) {
            dto.setPid(0l);
            return;
        }
        Optional.ofNullable(dto).ifPresent(c -> {
            CategoryVO parent = selectOne(c.getPid());
            Optional.ofNullable(parent).orElseThrow(() -> new ShopException(71003));
            switch (level) {
                case 2:
                    Optional.ofNullable(level).filter(l -> CategoryLevel.LEVEL1.getId() == parent.getLevel().intValue()).orElseThrow(() -> new ShopException(71005));
                    break;
                case 3:
                    Optional.ofNullable(level).filter(l -> CategoryLevel.LEVEL2.getId() == parent.getLevel().intValue()).orElseThrow(() -> new ShopException(71006));
                    break;
            }
            vo.setPidName(parent.getName());
        });
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
                return iSeqIncrService.nextVal("category_leve1_id", 6, Align.LEFT);
            case 2:
                return iSeqIncrService.nextVal("category_leve2_id", 6, Align.LEFT);
            case 3:
                return iSeqIncrService.nextVal("category_leve3_id", 6, Align.LEFT);
        }
        return null;
    }

}
