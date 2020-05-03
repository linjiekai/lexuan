package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.BrandGoods;
import com.zhuanbo.core.entity.Goods;

import java.util.List;

/**
 * <p>
 * 品牌商品关联表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-07-26
 */
public interface IBrandGoodsService extends IService<BrandGoods> {

    /**
     * 获取品牌关联商品
     * @param brandId
     * @return
     */
    List<Goods> findGoodByBrandId(long brandId, Integer page, Integer limit);
}
