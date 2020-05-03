package com.zhuanbo.core.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 购买码规则
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuyInviteCodeRuleVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer ptLevel;// 面膜升级目标等级
    private List<Integer> effectiveZhuanboPtLevel;// 可以升级的面膜等级
    private Integer effectiveZhuanboDepositOrderType;// 可以升级的面膜等级的充值类型
    private Integer existYinLiDepositOrderType;// 非有效（普通）面膜用户且精油存在时的充值订单类型 - 499 - 一次操作
    private List<Integer> normalZhuanboAndNotExistYinLiDepositOrderType;// 普通面膜用户且不存在精油充值类型
    private Boolean offLineBuy;// 是否线下交易
}
