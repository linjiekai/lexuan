package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuanbo.core.entity.UpgradeDetails;
import com.zhuanbo.service.vo.UpgradeDetailsVo;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 升级费明细表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2019-07-31
 */
public interface UpgradeDetailsMapper extends BaseMapper<UpgradeDetails> {

    /**
     * 管理后台获取升级费明细列表
     * @param page
     * @param limit
     * @param userId
     * @param mobile
     * @return
     */
    @Select("<script> select ud.id, ud.user_id,ud.price,ud.pay_date,ud.pay_time,ud.pay_type,ud.refund_flag" +
            ",ud.operator,ud.update_time,ud.pay_date,ud.pay_time,u.mobile,u.pt_level,u.user_name" +
            " from shop_upgrade_details as ud left join shop_user as u on ud.user_id=u.id where 1=1" +
            "<if test='userId!=null'> and ud.user_id=#{userId}</if>" +
            "<if test='mobile!=null'> and u.mobile like CONCAT('%',#{mobile},'%')</if>" +
            " order by ud.id desc" +
            " limit #{page},#{limit} " +
            "</script> ")
    List<UpgradeDetailsVo> findUpgradeDetails(Integer page, Integer limit, Long userId, String mobile);


    /**
     * 统计总记录数
     * @param userId
     * @param mobile
     * @return
     */
    @Select("<script> select count(ud.id) as countTotal " +
            " from shop_upgrade_details as ud left join shop_user as u on ud.user_id=u.id where 1=1" +
            "<if test='userId!=null'>and  ud.user_id=#{userId}</if>" +
            "<if test='mobile!=null'>and u.mobile like CONCAT('%',#{mobile},'%')</if>" +
            "</script> ")
    Integer countUpgradeTotalRecords(Long userId, String mobile);

    /**
     * 统计直属达人的数量
     * @param userId
     * @return
     */
    @Select("select count(*) from  shop_user_invite as sui left join shop_user as su on sui.id=su.id where sui.pid=#{userId} and su.pt_level=1")
    Integer countDarenNum(Long userId);

    @Select("select u.mobile,u.pt_level,u.user_name,u.id as userId from shop_user as u where u.mobile like CONCAT('%',#{mobile},'%')")
    List<UpgradeDetailsVo> findUserByMobile(String mobile);
}
