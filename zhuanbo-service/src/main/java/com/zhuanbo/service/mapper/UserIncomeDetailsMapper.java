package com.zhuanbo.service.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.entity.UserIncomeDetails;
import com.zhuanbo.service.vo.MarginVO;
import com.zhuanbo.service.vo.UserIncomeDetailsStatVO;
import com.zhuanbo.service.vo.UserIncomeDetailsVO;

/**
 * <p>
 * 用户收益明细表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
public interface UserIncomeDetailsMapper extends BaseMapper<UserIncomeDetails> {

	@Select("<script> "
			+"select u.head_img_url, u.nickname, i.id, i.operate_income, i.operate_type, i.income_type, i.income_date, i.income_time ,i.stat_type, i.price, i.content, i.from_user_id, i.add_time, i.change_type "
			+ " from shop_user u, shop_user_income_details i where u.id=i.from_user_id and i.status = 1"
			+ " <if test='params.userId != null'> and i.user_id = #{params.userId} </if> "
			+ " <if test='params.statType != null'> and i.stat_type = #{params.statType} </if> "
			+ " <if test='params.startDate != null and params.startDate != \"\"'> and i.income_date &gt;= #{params.startDate} </if> "
			+ " <if test='params.endDate != null and params.endDate != \"\"'> and i.income_date &lt;= #{params.endDate} </if> "
			+ " <if test='params.operateType != null'> and i.operate_type = #{params.operateType} </if> "
			+ " <if test='params.status != null'> and i.status = #{params.status} </if> "
			+ " <if test='params.incomeTypes != null and params.incomeTypes.size() > 0'> and i.income_type in <foreach collection=\"params.incomeTypes\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if> "
			+ " <if test='params.changeTypes != null and params.changeTypes.size() > 0'> and i.change_type in <foreach collection=\"params.changeTypes\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if> "
			+ " <if test='params.fromUserIds != null and params.fromUserIds.size() > 0'> and i.from_user_id in <foreach collection=\"params.fromUserIds\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if> "
			+ " order by i.id desc "
			+ "</script> "
	)
	List<UserIncomeDetailsVO> listMap(Page<UserIncomeDetailsVO> page, @Param("params") Map<String, Object> params);

	@Select("<script> "
			+"select sum(i.operate_income) operate_income"
			+ " from shop_user_income_details  where 1=1 "
			+ " <if test='params.userId != null'> and user_id = #{params.userId} </if> "
			+ " <if test='params.statType != null'> and stat_type = #{params.statType} </if> "
			+ " <if test='params.startDate != null'> and income_date &gt;= #{params.startDate} </if> "
			+ " <if test='params.endDate != null'> and income_date &lt;= #{params.endDate} </if> "
			+ " <if test='params.incomeTypes != null and params.incomeTypes.size() > 0'> and income_type in <foreach collection=\"params.incomeTypes\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if> "
			+ " <if test='params.fromUserIds != null and params.fromUserIds.size() > 0'> and from_user_id in <foreach collection=\"params.fromUserIds\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if> "
			+ " order by i.id desc "
			+ "</script> "
	)
	BigDecimal totalIncome(@Param("params") Map<String, Object> params);

	@Select("<script> "
			+"select count(1) income_count, sum(operate_income) operate_income"
			+ " from shop_user_income_details where 1=1 "
			+ " <if test='params.userId != null'> and user_id = #{params.userId} </if> "
			+ " <if test='params.statType != null'> and stat_type = #{params.statType} </if> "
			+ " <if test='params.incomeDate != null'> and income_date = #{params.incomeDate} </if> "
			+ " <if test='params.startDate != null'> and income_date &gt;= #{params.startDate} </if> "
			+ " <if test='params.endDate != null'> and income_date &lt;= #{params.endDate} </if> "
			+ " <if test='params.incomeTypes != null and params.incomeTypes.size() > 0'> and income_type in <foreach collection=\"params.incomeTypes\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if> "
			+ " <if test='params.fromUserIds != null and params.fromUserIds.size() > 0'> and from_user_id in <foreach collection=\"params.fromUserIds\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if> "
			+ "</script> "
	)
	UserIncomeDetailsStatVO incomeStat(@Param("params") Map<String, Object> params);

	@Select("<script> "
			+"select count(1) income_count, sum(operate_income) operate_income, income_type,  operate_type"
			+ " from shop_user_income_details where 1=1 and status = 1 "
			+ " <if test='params.userId != null'> and user_id = #{params.userId} </if> "
			+ " <if test='params.statType != null'> and stat_type = #{params.statType} </if> "
			+ " <if test='params.incomeDate != null'> and income_date = #{params.incomeDate} </if> "
			+ " <if test='params.startDate != null'> and income_date &gt;= #{params.startDate} </if> "
			+ " <if test='params.operateType != null'> and operate_type = #{params.operateType} </if> "
			+ " <if test='params.endDate != null'> and income_date &lt;= #{params.endDate} </if> "
			+ " <if test='params.incomeTypes != null and params.incomeTypes.size() > 0'> and income_type in <foreach collection=\"params.incomeTypes\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if> "
			+ " <if test='params.fromUserIds != null and params.fromUserIds.size() > 0'> and from_user_id in <foreach collection=\"params.fromUserIds\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if> "
			+ " group by income_type,  operate_type"
			+ "</script> "
	)
	List<UserIncomeDetailsStatVO> incomeStatGruopBy(@Param("params") Map<String, Object> params);

	@Select("<script> " +
			"SELECT a.id, a.order_no orderNo, a.user_id userId, b.name userName, c.auth_no authNo, " +
			"a.change_type changeType, a.operate_type operateType, a.price, d.bank_code bankCode, a.add_time addTime, e.operator " +
			"FROM shop_user_income_details a " +
			"LEFT JOIN shop_user b ON b.id=a.user_id AND b.deleted=0 " +
			"LEFT JOIN shop_user_partner c ON c.user_id=a.user_id AND c.auth_status=1 AND c.auth_type='B' " +
			"LEFT JOIN shop_deposit_order d ON d.order_no=a.order_no " +
			"LEFT JOIN shop_user_income_adjust e ON e.adjust_no=a.order_no " +
			"WHERE 1=1 AND a.income_type=8 and a.status = 1" +
			"<if test='params.userId != null'>AND a.user_id = #{params.userId} </if>" +
			"<if test='params.authNo != null'>AND c.auth_no = #{params.authNo} </if>" +
			"<if test='params.userName != null'>AND c.name = #{params.userName} </if>" +
			"<if test='params.startDate != null'>AND a.add_time &gt;= #{params.startDate} </if>" +
			"<if test='params.endDate != null'>AND a.add_time &lt;= #{params.endDate} </if>" +
			"ORDER BY a.add_time desc </script>")
	List<MarginVO> listMargin(Page<MarginVO> page, @Param("params") Map<String, Object> params);

}
