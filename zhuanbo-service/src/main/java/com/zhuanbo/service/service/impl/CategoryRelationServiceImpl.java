package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.CategoryRelation;
import com.zhuanbo.service.mapper.CategoryRelationMapper;
import com.zhuanbo.service.service.ICategoryRelationService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 类目关联表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-07-03
 */
@Service
public class CategoryRelationServiceImpl extends ServiceImpl<CategoryRelationMapper, CategoryRelation> implements ICategoryRelationService {

}
