package com.zhuanbo.service.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.StatIncomeDay;
import com.zhuanbo.core.entity.StatUserSaleDay;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
public interface OrderMapper extends BaseMapper<Order> {

    @Select("<script>" +
            " select o.* from shop_order o inner join shop_order_goods og on og.order_no = o.order_no   " +
            "<if test='ew.supplier_code != null and ew.supplier_code != \"\"'> inner join shop_goods gg on gg.id = og.goods_id </if>" +
            " where 1 = 1 " +
            "<if test='ew.order_no != null and ew.order_no != \"\"'> and o.order_no like #{ew.order_no} </if>" +
            "<if test='ew.order_status != null and ew.order_status != \"\"'> and o.order_status = #{ew.order_status} </if>" +
            "<if test='ew.user_id != null and ew.user_id != \"\"'> and o.user_id = #{ew.user_id} </if>" +
            "<if test='ew.goods_id != null and ew.goods_id != \"\"'> and og.goods_id like #{ew.goods_id} </if>" +
            "<if test='ew.supplier_code != null and ew.supplier_code != \"\"'> and gg.supplier_code = #{ew.supplier_code} </if>" +
            "<if test='ew.startDate != null'> AND o.order_date &gt;= #{ew.startDate} </if>" +
            "<if test='ew.endDate != null'> AND o.order_date &lt;= #{ew.endDate} </if>" +
            "<if test='ew.inviteUserId != null'> AND o.invite_user_id = #{ew.inviteUserId} </if>" +
            "<if test='ew.mobileUserId != null'> AND o.user_id = #{ew.mobileUserId} </if>" +
            " group by o.id order by o.add_time desc </script>")
    List<Order> orderList(Page<Order> page, @Param("ew") Map<String, Object> ew);


    /**
     * 订单列表，自定义查询条件
     * @param page
     * @param ew
     * @return
     */
    @Select("<script> select o.* from shop_order o where 1 = 1 " +
            " <if test='ew.userId != null and ew.userId != \"\"'> and o.user_id = #{ew.userId} </if>" +
            " <if test='ew.orderStatus != null and ew.orderStatus != \"\"'> and o.order_status = #{ew.orderStatus} </if>" +
            " <if test='ew.orderStatus != null and ew.orderStatus == \"W\"'> and o.exp_time > #{ew.now} </if>" +
            " order by o.id desc " +
            "</script>")
    List<Order> pageCustom(Page<Order> page, Map<String, Object> ew);
    

    @Select("<script>" 
    		+ " select count(1) vs_order_count,sum(price) vs_order_price from shop_order where "
    		+ " order_status in ('WS','WD','S') and pay_date=#{params.statDate}"
    		+ " <if test='params.userId != null'> and user_id=#{params.userId} </if> "
    		+ " <if test='params.userIds != null'> and user_id in <foreach collection=\"params.userIds\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if> "
            + " </script>")
    StatIncomeDay statUserSale(@Param("params") Map<String, Object> params);

    @Select("<script>"
            + " select count(1) vs_order_count,user_id vs_user_id,sum(price) vs_order_price from shop_order where "
            + " order_status in ('WS','WD','S') and pay_date=#{statDate} group by user_id"
            + " </script>")
	List<StatUserSaleDay> listUserSale(String statDate);

    @Select("select og.buyer_partner p  from shop_order o inner join shop_order_goods og on og.order_no = o.order_no where o.user_id = #{uid} and og.goods_type = 1 and o.order_no != #{ignoreOrderNo} and o.order_status in ('S','WS','WD');")
    List<Integer> giftOrderNumber(@Param("uid") Long uid, @Param("ignoreOrderNo") String ignoreOrderNo);
}
