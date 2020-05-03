package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.UserPartnerProfitRule;

import java.math.BigDecimal;

/**
 * <p>
 * 利润分配规则表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-08-20
 */
public interface IUserPartnerProfitRuleService extends IService<UserPartnerProfitRule> {


    /**
     * 自买省差价计算
     *
     * @param orgPrice 原订单金额
     * @return
     */
    BigDecimal priceDiffForSelfPurchase(int ptLevel, BigDecimal orgPrice);

}
