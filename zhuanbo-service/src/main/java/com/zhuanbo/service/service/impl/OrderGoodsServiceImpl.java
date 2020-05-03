package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.OrderGoods;
import com.zhuanbo.service.mapper.OrderGoodsMapper;
import com.zhuanbo.service.service.IOrderGoodsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 订单商品表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@Service
public class OrderGoodsServiceImpl extends ServiceImpl<OrderGoodsMapper, OrderGoods> implements IOrderGoodsService {

    @Override
    public List<OrderGoods> findByOrderNo(String orderNo) {
        QueryWrapper<OrderGoods> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        return list(queryWrapper);
    }

    @Override
    public List<OrderGoods> listForWD(LocalDateTime time) {
        return baseMapper.listForWD(time);
    }

    @Override
    public Integer buyNumCount(String orderNo, Integer goodsType) {
        return baseMapper.buyNumCount(orderNo, goodsType);
    }

    @Override
    public List<String> getOrderNoListByGoodsId(Long goodsId) {
        return baseMapper.getOrderNoListByGoodsId(goodsId);
    }
}
