package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.entity.StatUserSaleDay;
import com.zhuanbo.service.vo.StatUserTeamVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户销量统计天报表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2019-06-27
 */
public interface StatUserSaleDayMapper extends BaseMapper<StatUserSaleDay> {

	@Select("<script> "
			+"select u.head_img_url, u.nickname, i.vs_user_id user_id, i.vs_order_price price, i.vs_order_count count, i.stat_date stat_date"
			+ " from shop_user u, (select vs_user_id,sum(vs_order_price) vs_order_price, sum(vs_order_count) vs_order_count,max(stat_date) stat_date from stat_user_sale_day where 1=1 "
			+ " <if test='params.userIds != null'> and vs_user_id in <foreach collection=\"params.userIds\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if> "
			+ " group by vs_user_id ) i where u.id=i.vs_user_id "
			+ " order by i.stat_date desc " 
			+ "</script> " 
			)
	List<StatUserTeamVO> statUserSale(Page<StatUserTeamVO> page, @Param("params") Map<String, Object> params);

	@Select("<script> "
			+"select sum(vs_order_price) price, sum(vs_order_count) count from stat_user_sale_day where 1=1 "
			+ " <if test='params.statDate != null'> and stat_date=#{params.statDate} </if> "
			+ " <if test='params.userId != null'> and vs_user_id=#{params.userId} </if> "
			+ " <if test='params.userIds != null'> and vs_user_id in <foreach collection=\"params.userIds\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if> "
			+ " <if test='params.ptLevels != null'> and vs_pt_level in <foreach collection=\"params.ptLevels\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if> "
			+ "</script> " 
			)
	StatUserTeamVO statUserSaleTotal(@Param("params") Map<String, Object> params);

	@Select("<script> "
			+"select sum(vs_order_price) price, sum(vs_order_count) count from stat_user_sale_day where 1=1 "
			+ " <if test='params.statDate != null'> and stat_date=#{params.statDate} </if> "
			+ " <if test='params.userId != null'> and vs_user_id=#{params.userId} </if> "
			+ "</script> " 
			)
	StatUserTeamVO statUserConsumeTotal(@Param("params") Map<String, Object> params);

}
