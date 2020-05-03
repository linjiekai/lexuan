package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.entity.GoodsCarriage;
import com.zhuanbo.service.mapper.GoodsCarriageMapper;
import com.zhuanbo.service.service.IGoodsCarriageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Service
public class GoodsCarriageServiceImpl extends ServiceImpl<GoodsCarriageMapper, GoodsCarriage>  implements IGoodsCarriageService {

    @Resource
    private AuthConfig authConfig;

    @Override
    public BigDecimal findOne(Integer type) {
        // type暂时用不上
        GoodsCarriage one = getOne(null);
        return one == null ? BigDecimal.ZERO : one.getPrice();
    }

    @Override
    public BigDecimal calculateShipping(Integer num) {
        // 运费=运费模板价格✖购买数量/3
        BigDecimal shipPrice = findOne(null);
        shipPrice = shipPrice.multiply(new BigDecimal(String.valueOf(num))).divide(new BigDecimal(authConfig.getShipGoodsNumber()), 2,BigDecimal.ROUND_HALF_UP);
        return shipPrice;
    }
}
