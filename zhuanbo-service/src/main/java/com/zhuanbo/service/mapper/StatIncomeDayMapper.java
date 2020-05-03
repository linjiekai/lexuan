package com.zhuanbo.service.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuanbo.core.entity.StatIncomeDay;

/**
 * <p>
 * 收益日统计报表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2019-06-20
 */
public interface StatIncomeDayMapper extends BaseMapper<StatIncomeDay> {
	@Select("<script> " +
            " SELECT pt_level AS level, COUNT(*) AS count FROM shop_user a WHERE 1=1 " +
            " <if test='ew.startDate != null and ew.startDate != \"\" '> AND a.add_time &gt;= #{ew.startDate} </if> " +
            " <if test='ew.endDate != null and ew.endDate != \"\" '> AND a.add_time &lt;= #{ew.endDate} </if> " +
            " <if test='ew.ptLevel != null '> AND a.pt_level in <foreach collection=\"ew.ptLevel\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if> " +
            " GROUP BY level " +
            " </script>")
    List<Map<String,Object>> statUserCount(@Param("ew") Map<String, Object> ew);

    @Select("<script> " +
            " SELECT a.new_level AS level, COUNT(*) AS count FROM shop_level_change_record a WHERE 1=1 " +
            " <if test='ew.startDate != null and ew.startDate != \"\" '> AND a.add_time &gt;= #{ew.startDate} </if> " +
            " <if test='ew.endDate != null and ew.endDate != \"\" '> AND a.add_time &lt;= #{ew.endDate} </if> " +
            " <if test='ew.ptLevel != null '> AND a.new_level in <foreach collection=\"ew.ptLevel\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if> " +
            " GROUP BY level " +
            " </script>")
    List<Map<String,Object>> statUserLevelCount(@Param("ew") Map<String, Object> ew);

    @Select("<script> " +
            " SELECT pt_level AS level, a.reg_date AS date, COUNT(*) AS count FROM shop_user a WHERE 1=1 " +
            " <if test='ew.startDate != null and ew.startDate != \"\" '> AND a.add_time &gt;= #{ew.startDate} </if> " +
            " <if test='ew.endDate != null and ew.endDate != \"\" '> AND a.add_time &lt;= #{ew.endDate} </if> " +
            " <if test='ew.ptLevel != null '> AND a.pt_level in <foreach collection=\"ew.ptLevel\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if> " +
            " GROUP BY level, date " +
            " </script>")
    List<Map<String,Object>> statUserCountByDate(@Param("ew") Map<String, Object> ew);

    @Select("<script> " +
            " SELECT a.new_level AS level, DATE_FORMAT(a.add_time,'%Y-%m-%d') AS date, COUNT(*) AS count FROM shop_level_change_record a WHERE 1=1 " +
            " <if test='ew.startDate != null and ew.startDate != \"\" '> AND a.add_time &gt;= #{ew.startDate} </if> " +
            " <if test='ew.endDate != null and ew.endDate != \"\" '> AND a.add_time &lt;= #{ew.endDate} </if> " +
            " <if test='ew.ptLevel != null '> AND a.new_level in <foreach collection=\"ew.ptLevel\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if> " +
            " GROUP BY level, date " +
            " </script>")
    List<Map<String,Object>> statUserLevelByDate(@Param("ew") Map<String, Object> ew);
}
