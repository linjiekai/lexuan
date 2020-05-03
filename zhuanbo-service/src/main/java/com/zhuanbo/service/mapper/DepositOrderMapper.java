package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhuanbo.core.entity.DepositOrder;
import com.zhuanbo.service.vo.DepositOrderVO;
import com.zhuanbo.service.vo.StatDepositOrderGroupByVo;
import com.zhuanbo.service.vo.StatDepositOrderVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


/**
 * <p>
 * 充值订单表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
public interface DepositOrderMapper extends BaseMapper<DepositOrder> {

	@Select("<script> select su.nickname as nickname, sd.bank_code,sd.pay_no, sd.user_id, sd.price, sd.order_status, sd.pay_date, sd.pay_time from shop_deposit_order  sd , shop_user  su "
            + "  where  sd.user_id=su.id and sd.busi_type=#{ew.busi_type} AND sd.order_status=#{ew.order_status}"
            + " <if test='ew.user_id != null'> and sd.user_id = #{ew.user_id} </if>"
            + " <if test='ew.pay_no != null'> and sd.pay_no = #{ew.pay_no} </if>"
            + "AND order_date &lt;= #{ew.endPayDate}  AND order_date &gt;= #{ew.startPayDate}  order by sd.pay_date desc limit #{ew.limitStart},#{ew.limitEnd}"
            + "</script>")
    List<DepositOrderVO> selectDepositOrderList(@Param("ew")Map<String, Object> ew);

    @Select("<script> select su.nickname as nickname,su.name as name,su.mobile as mobile, sd.* from shop_deposit_order sd join shop_user su on sd.user_id=su.id and su.status=1 "
           + "  where 1=1 "
            + " <if test='ew.busiType != null and ew.busiType != \"\" '> and sd.busi_type = #{ew.busiType} </if>"
            + " <if test='ew.userId != null and ew.userId != \"\" '> and sd.user_id = #{ew.userId} </if>"
            + " <if test='ew.name != null and ew.name != \"\" '> and su.name = #{ew.name} </if>"
            + " <if test='ew.mobile != null and ew.mobile != \"\" '> and su.mobile = #{ew.mobile} </if>"
            + " <if test='ew.tradeCode != null and ew.tradeCode != \"\" '> and sd.trade_code = #{ew.tradeCode} </if>"
            + " <if test='ew.payNo != null and ew.payNo != \"\" '> and sd.pay_no = #{ew.payNo} </if>"
            + " <if test='ew.orderStatus != null'> and sd.order_status in <foreach collection=\"ew.orderStatus\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if> "
            + " <if test='ew.endPayDate != null'> AND sd.order_date &lt;= #{ew.endPayDate} </if>"
            + " <if test='ew.startPayDate != null'> AND sd.order_date &gt;= #{ew.startPayDate} </if>"
            + " order by sd.pay_date desc,sd.pay_time desc "
            + "</script>")
    List<DepositOrderVO> selectExList(IPage<DepositOrderVO> page, @Param("ew")Map<String, Object> ew);

    @Select("<script> select sum(price) vs_deposit_price, count(1) vs_deposit_count from shop_deposit_order where 1=1 "
    		+ " <if test='params.orderStatus != null'> and order_status = #{params.orderStatus} </if>"
    		+ " <if test='params.busiType != null'> and busi_type = #{params.busiType} </if>"
            + " <if test='params.payDate != null'> and pay_date = #{params.payDate} </if>"
            + "</script>")
    public StatDepositOrderVo statDepositOrder(@Param("params") Map<String, Object> params);

    @Select("<script> select sum(price) vs_deposit_price, count(1) vs_deposit_count, order_type from shop_deposit_order where 1=1 "
    		+ " <if test='params.orderStatus != null'> and order_status = #{params.orderStatus} </if>"
    		+ " <if test='params.busiType != null'> and busi_type = #{params.busiType} </if>"
            + " <if test='params.payDate != null'> and pay_date = #{params.payDate} </if>"
            + " GROUP BY order_type </script>")
    public List<StatDepositOrderGroupByVo> statDepositOrderGroupBy(@Param("params") Map<String, Object> params);

    @Select("<script> select su.nickname as nickname, su.head_img_url as head_img_url, su.name as name,su.mobile as mobile, sd.bank_code,sd.pay_no, sd.user_id, sd.price, sd.order_status, sd.pay_date, sd.pay_time,sd.order_no from shop_deposit_order  sd , shop_user  su "
            + "  where  sd.user_id=su.id  "
            + " <if test='ew.orderStatus != null'> and sd.order_status=#{ew.orderStatus} </if>"
            + " <if test='ew.busiType != null'> and sd.busi_type=#{ew.busiType} </if>"
            + " <if test='ew.userId != null'> and sd.user_id = #{ew.userId} </if>"
            + " <if test='ew.userIdName != null'> and sd.user_id = #{ew.userIdName} </if>"
            + " <if test='ew.userIdMobile != null'> and sd.user_id = #{ew.userIdMobile} </if>"
            + " <if test='ew.tradeCode != null'> and sd.trade_code = #{ew.tradeCode} </if>"
            + " <if test='ew.payNo != null'> and sd.pay_no = #{ew.payNo} </if>"
            + " order by sd.pay_date desc,sd.pay_time desc"
            + "</script>")
    List<DepositOrderVO> selectDepositOrderListOfShop(IPage page, @Param("ew")Map<String, Object> ew);

    @Select("<script> SELECT SUM(a.price) FROM shop_deposit_order a WHERE a.trade_code='02' AND a.busi_type='02' AND a.order_status='S' </script>")
    public String sumExList();
    
}
