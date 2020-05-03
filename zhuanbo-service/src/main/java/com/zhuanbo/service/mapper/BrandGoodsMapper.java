package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuanbo.core.entity.BrandGoods;
import com.zhuanbo.core.entity.Goods;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 品牌商品关联表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2019-07-26
 */
public interface BrandGoodsMapper extends BaseMapper<BrandGoods> {

    /**
     * 获取品牌关联商品
     * @param brandId
     * @return
     */
    @Select("select g.id,g.name,g.alias,g.cover_images from shop_goods as g  where g.brand_id=#{brandId} and g.status=1 and g.deleted=0 order by  g.id desc limit #{page},#{limit} ")
    List<Goods> findGoodByBrandId(@Param("brandId") long brandId, @Param("page") Integer page, @Param("limit") Integer limit);
}
