package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.constants.UserPartnerProfitRuleTypeEnum;
import com.zhuanbo.core.entity.UserPartnerProfitRule;
import com.zhuanbo.service.mapper.UserPartnerProfitRuleMapper;
import com.zhuanbo.service.service.IUserPartnerProfitRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


/**
 * <p>
 * 利润分配规则表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-08-20
 */
@Slf4j
@Service
public class UserPartnerProfitRuleServiceImpl extends ServiceImpl<UserPartnerProfitRuleMapper, UserPartnerProfitRule> implements IUserPartnerProfitRuleService {


    /**
     * 自买省差价计算
     *
     * @param orgPrice 原订单金额
     * @return
     */
    @Override
    public BigDecimal priceDiffForSelfPurchase(int ptLevel, BigDecimal orgPrice) {
        log.info("|自买省差价计算|用户等级:{}, 原订单价格:{}", ptLevel, orgPrice);
        UserPartnerProfitRule profitRule = this.getOne(new QueryWrapper<UserPartnerProfitRule>().eq("profit_type", UserPartnerProfitRuleTypeEnum.SELF_PURCHASE_SAVE.getId()));
        BigDecimal priceRatio = BigDecimal.ZERO;
        BigDecimal vip = profitRule.getVip();
        BigDecimal storeManager = profitRule.getStoreManager();
        BigDecimal director = profitRule.getDirector();
        BigDecimal partner = profitRule.getPartner();
        BigDecimal base = profitRule.getBase();
        switch (ptLevel) {
            case 1:
                priceRatio = priceRatio.add(vip);
                break;
            case 2:
                priceRatio = priceRatio.add(vip).add(storeManager);
                break;
            case 3:
                priceRatio = priceRatio.add(vip).add(storeManager).add(director);
                break;
            case 4:
                priceRatio = priceRatio.add(vip).add(storeManager).add(director).add(partner);
                break;
            case 5:
                priceRatio = priceRatio.add(vip).add(storeManager).add(director).add(partner).add(base);
                break;
            default:
                log.info("|自买省差价计算|用户等级无效,用户等级:{}", ptLevel);
                break;
        }
        BigDecimal priceDiff = orgPrice.multiply(priceRatio);
        log.info("|自买省差价计算|用户等级:{}, 原订单价格:{}, 差价:{}", ptLevel, orgPrice, priceDiff);
        return priceDiff;
    }
}
