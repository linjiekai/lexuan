package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.OrderGoods;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 订单商品表 服务类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
public interface IOrderGoodsService extends IService<OrderGoods> {

    /**
     * 根据订单Id获取相关记录
     * @param orderNo 订单编号
     * @return
     */
    List<OrderGoods> findByOrderNo(String orderNo);

    /**
     * 查找一定时间范围内的待收货订单
     * @param time 时间范围
     * @return
     */
    List<OrderGoods> listForWD(@Param("time") LocalDateTime time);

    /**
     * 商品购买数量
     *
     * @param orderNo 订单编号
     * @return
     */
    Integer buyNumCount(String orderNo, Integer goodsType);

    List<String> getOrderNoListByGoodsId(Long goodsId);
}
