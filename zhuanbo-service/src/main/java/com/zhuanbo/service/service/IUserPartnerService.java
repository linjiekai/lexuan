package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserPartner;

/**
 * <p>
 * 合伙人基础信息表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-08-19
 */
public interface IUserPartnerService extends IService<UserPartner> {

    /**
     * 更新某用户运营商邀请永久有效会员数量
     * @param id 某用户id
     * @param number 数量
     * @return
     */
    int updatePtEffNum(Long id, int number);

    /**
     * 简单生成一个
     * @param user
     * @return
     */
    Long simpleGenerate(User user);

    /**
     * 更新名额，小于才生效20
     * @param id 用户id
     * @param number 名额数量
     * @param limit 限制的数量
     * @return
     */
    int updatePtEffNumMax20(Long id, int number, long limit);

    /**
     * 判断用户是否有过某些礼包
     * @param id
     * @param types
     * @return
     */
    int haveTypes(Long id, Integer... types);

    /**
     * 判断用户是否有过某些礼包
     * @param userPartner
     * @param types
     * @return
     */
    int haveTypes(UserPartner userPartner, Integer... types);

    /**
     * 更新用户的purchased_type
     * @param uid 用户id
     * @param purchasedType
     * @return
     */
    int updatePurchasedType(Long uid, Integer purchasedType);

    /**
     * 更新授权日期与购买类型
     * @param userPartner
     * @return
     */
    int updateAuthDateAntType(UserPartner userPartner, Integer purchasedType);

    /**
     * 生成授权编号
     * @param user 用户
     */
    void generateAutoNo(User user);
}
