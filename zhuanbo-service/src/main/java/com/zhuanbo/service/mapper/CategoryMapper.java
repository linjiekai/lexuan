package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuanbo.core.entity.Category;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 商品类目表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2019-07-03
 */
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("SELECT id from shop_category t, (SELECT @DATAS := getChildId_from_category (#{id})) a WHERE find_in_set (ID, @DATAS);")
    public List<Long> queryCateIds(Long id);
}
