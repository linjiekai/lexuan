package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuanbo.core.dto.AdminWithdrDTO;
import com.zhuanbo.core.entity.WithdrOrder;
import com.zhuanbo.service.vo.WithdrOrderExportVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 提现订单表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
public interface WithdrOrderMapper extends BaseMapper<WithdrOrder> {

	/**
     * 提现订单导出数据查询
     *
     * @param withdrDTO
     * @return
     */
    @Select(" <script> " +
            " SELECT w.user_id,w.order_no,w.order_status,w.price, i.total_income,i.total_uava_income, w.bank_withdr_date, w.bank_withdr_time, w.add_time, u.name as username " +
            " FROM shop_withdr_order w, shop_user u, shop_user_income i, shop_dictionary d " +
            " WHERE w.user_id = u.id AND w.user_id  = i.user_id AND w.bank_code = d.str_val AND d.category = 'bankcode' " +
            " <if test='withdrDTO.orderStatus != null and withdrDTO.orderStatus != \"\" '> AND w.order_status = #{withdrDTO.orderStatus} </if>" +
            " <if test='withdrDTO.userId != null and withdrDTO.userId != \"\" '> AND w.user_id = #{withdrDTO.userId}   </if>" +
            " <if test='withdrDTO.nickname != null and withdrDTO.nickname != \"\" '> AND u.nickname LIKE '%${withdrDTO.nickname}%' </if>" +
            " <if test='withdrDTO.startTime != null and withdrDTO.startTime != \"\" '> AND w.add_time <![CDATA[ >= ]]> #{withdrDTO.startTime} </if>" +
            " <if test='withdrDTO.endTime != null and withdrDTO.endTime != \"\" '> AND w.add_time <![CDATA[ <= ]]> #{withdrDTO.endTime} </if>" +
            " ORDER BY w.add_time DESC " +
            " </script> ")
    List<WithdrOrderExportVO> exportOrder(@Param("withdrDTO") AdminWithdrDTO withdrDTO);

}

