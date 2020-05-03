package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.entity.AdjustAccount;
import com.zhuanbo.service.vo.AdjustAccountVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 调怅记录表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2019-09-24
 */
public interface AdjustAccountMapper extends BaseMapper<AdjustAccount> {

    @Select("<script>" +
            " select aa.*, u.nickname, u2.nickname adjust_user_nickname from shop_user_income_adjust aa inner join shop_user u on u.id = aa.user_id" +
            " inner join shop_user u2 on u2.id = aa.adjust_user_id" +
            " where 1 = 1" +
            "<if test='ew.userId != null'> and u.id = #{ew.userId}</if>" +
            "<if test='ew.nickname != null'> and u.nickname = #{ew.nickname}</if>" +
            "<if test='ew.orderNo != null'> and aa.order_no like #{ew.orderNo}</if>" +
            " order by aa.id desc" +
            "</script>")
    List<AdjustAccountVO> list(Page<AdjustAccountVO> page, @Param("ew") Map<String, Object> params);
    
    @Select("<script> SELECT * FROM shop_user_income_details_bak_20191221  </script>")
    List<Map<String,Object>> listTemp();
}
