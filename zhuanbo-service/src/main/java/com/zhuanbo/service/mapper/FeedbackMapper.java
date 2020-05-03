package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.entity.Feedback;
import com.zhuanbo.service.vo.FeedbackVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 反馈表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2019-01-14
 */
public interface FeedbackMapper extends BaseMapper<Feedback> {

    /**
     * 管理后台列表
     * @param id
     * @param startDate
     * @param endDate
     * @return
     */
    @Select("<script> " +
            " select f.id,u.id userId,u.user_name userName,u.head_img_url userHeader, f.content,f.mobile_model mobileModel," +
            " f.mobile_system_version mobileSystemVersion,f.network,f.app_version appVersion,f.add_time addTime, f.images" +
            " from shop_feedback f left join shop_user u on u.id = f.user_id where 1 = 1 " +
            " <if test='id != null'> and u.id = #{id} </if> " +
            " <if test='platform != null'> and f.platform = #{platform} </if> " +
            " <if test='startDate != null'> and f.add_time &gt;= #{startDate} </if> " +
            " <if test='endDate != null'> and f.add_time &lt;= #{endDate} </if> " +
            " order by f.add_time desc </script>")
    List<FeedbackVO> listMap(Page<FeedbackVO> page, @Param("id") Long id, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("platform") String platform);
}
