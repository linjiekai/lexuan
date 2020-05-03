package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.BrandGoods;
import com.zhuanbo.core.entity.Goods;
import com.zhuanbo.service.mapper.BrandGoodsMapper;
import com.zhuanbo.service.service.IBrandGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 品牌商品关联表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-07-26
 */
@Service
public class BrandGoodsServiceImpl extends ServiceImpl<BrandGoodsMapper, BrandGoods> implements IBrandGoodsService {

    @Autowired
    private BrandGoodsMapper brandGoodsMapper;
    /**
     * 获取品牌关联商品
     * @param brandId
     * @return
     */
    @Override
    public List<Goods> findGoodByBrandId(long brandId,Integer page,Integer limit) {
        //获取数据
        return brandGoodsMapper.findGoodByBrandId(brandId,page,limit);
    }
}
