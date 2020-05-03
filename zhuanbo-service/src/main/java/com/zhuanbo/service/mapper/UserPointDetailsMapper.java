package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhuanbo.core.dto.UserPointDetailsDTO;
import com.zhuanbo.core.entity.UserPointDetails;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 用户积分明细表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
public interface UserPointDetailsMapper extends BaseMapper<UserPointDetails> {

    /**
     * 积分详情分页查询
     *
     * @param iPage
     * @param pointDetails
     * @return
     */
    @Select("<script>" +
            " SELECT d.*,u.nickname,u.mobile,p.auth_no,uf.mobile from_user_mobile FROM shop_user_point_details d " +
            " LEFT JOIN shop_user u ON d.user_id = u.id " +
            " LEFT JOIN shop_user_partner p ON d.user_id = p.id " +
            " LEFT JOIN shop_user uf on uf.id = d.from_user_id " +
            " WHERE 1=1 " +
            " <if test='pointDetails.userId != null and pointDetails.userId != \"\"'> AND d.user_id = #{pointDetails.userId} </if> " +
            " <if test='pointDetails.nickname != null and pointDetails.nickname != \"\"'> AND u.nickname = #{pointDetails.nickname} </if> " +
            " <if test='pointDetails.mobile != null and pointDetails.mobile != \"\"'> AND u.mobile = #{pointDetails.mobile} </if> " +
            " <if test='pointDetails.authNo != null and pointDetails.authNo != \"\"'> AND p.auth_no = #{pointDetails.authNo} </if> " +
            " <if test='pointDetails.fromUserId != null and pointDetails.fromUserId != \"\"'> AND d.from_user_id = #{pointDetails.fromUserId} </if> " +
            " <if test='pointDetails.operateType != null and pointDetails.operateType != \"\"'> AND d.operate_type = #{pointDetails.operateType} </if> " +
            " <if test='pointDetails.fromUserMobile != null and pointDetails.fromUserMobile != \"\"'> AND uf.mobile = #{pointDetails.fromUserMobile} </if> " +
            " ORDER BY d.add_time DESC " +
            "</script>")
    List<UserPointDetailsDTO> page(IPage iPage, @Param("pointDetails") UserPointDetailsDTO pointDetails);

    /**
     * 积分统计
     *
     * @return
     */
    @Select("<script>" +
            " SELECT point_type, SUM(operate_point) AS operate_point FROM shop_user_point_details" +
            " WHERE STATUS = 1" +
            " GROUP BY point_type " +
            "</script>")
    List<UserPointDetails> statisticPoint();

    /**
     * 积分日统计
     *
     * @return
     */
    @Select("<script>" +
            " SELECT point_date, SUM(operate_point) AS operate_point FROM shop_user_point_details " +
            " WHERE STATUS = 1" +
            " AND point_type = #{pointType} " +
            " AND DATE(add_time) >= #{addTime} " +
            " GROUP BY point_date " +
            " ORDER BY point_date " +
            "</script>")
    List<UserPointDetails> statisticPointByDay(@Param("pointType") Integer pointType,
                                               @Param("addTime") LocalDate addTime);
}
