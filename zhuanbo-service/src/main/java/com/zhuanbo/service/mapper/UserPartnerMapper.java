package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuanbo.core.entity.UserPartner;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 合伙人基础信息表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2019-08-19
 */
public interface UserPartnerMapper extends BaseMapper<UserPartner> {

    /**
     * 更新某用户运营商邀请永久有效会员数量
     * @param id 某用户id
     * @param number 数量
     * @return
     */
    @Update("update shop_user_partner set pt_eff_num = pt_eff_num + #{number} where id = #{id}")
    int updatePtEffNum(Long id, int number);

    /**
     * 更新名额，小于才生效20
     * @param id 用户id
     * @param number 名额
     * @return
     */
    @Update("update shop_user_partner set pt_eff_num = pt_eff_num + #{number} where id = #{id} and pt_eff_num < #{limit}")
    int updatePtEffNumMax20(@Param("id") Long id, @Param("number") int number, @Param("limit") long limit);

    /**
     * 更新用户的purchased_type
     * @param uid 用户id
     * @param purchasedType
     * @return
     */
    @Update("update shop_user_partner set purchased_type = purchased_type | #{purchasedType} where id = #{id} and (purchased_type & #{purchasedType}) = 0")
    int updatePurchasedType(@Param("id") Long uid, @Param("purchasedType") Integer purchasedType);

    /**
     * 更新授权日期与购买类型
     * @param userPartner
     * @return
     */
    @Update("update shop_user_partner set auth_no = #{userPartner.authNo}, auth_date = #{userPartner.authDate}, purchased_type = purchased_type | #{purchasedType} where id = #{userPartner.id}")
    int updateAuthDateAntType(@Param("userPartner") UserPartner userPartner, @Param("purchasedType") Integer purchasedType);
}
