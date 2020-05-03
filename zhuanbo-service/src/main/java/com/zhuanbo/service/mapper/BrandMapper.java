package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuanbo.core.entity.Brand;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 品牌商表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2019-07-26
 */
public interface BrandMapper extends BaseMapper<Brand> {

    /**
     * 获取所有全部品牌id
     * @return
     */
    @Select("select id from shop_brand where deleted=0 and status=1 order by indexs desc")
    List<Long> findBrandId();

}
