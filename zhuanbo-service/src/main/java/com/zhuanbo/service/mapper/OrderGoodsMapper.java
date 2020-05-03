package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuanbo.core.entity.OrderGoods;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 订单商品表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
public interface OrderGoodsMapper extends BaseMapper<OrderGoods> {

    /**
     * 查找一定时间范围内的待收货订单
     * @param time 时间范围
     * @return
     */
    @Select("select o.order_no, og.trace_type from shop_order o inner join shop_order_goods og on og.order_no = o.order_no where o.order_status = 'WD' and o.add_time < #{time}")
    List<OrderGoods> listForWD(@Param("time") LocalDateTime time);

    @Select("select sum(number) count from shop_order_goods where order_no=#{orderNo} and goods_type=#{goodsType}")
    Integer buyNumCount(@Param("orderNo")String orderNo, @Param("goodsType")Integer goodsType);

    /**
     * 查订单号列表
     *
     * @param goodsId
     * @return
     */
    @Select("select o.order_no from shop_order_goods o where o.deleted=0 and o.goods_id = #{goodsId}")
    List<String> getOrderNoListByGoodsId(@Param("goodsId") Long goodsId);
}
