package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 合伙人利润分配规则类型
 * @author Administrator
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum UserPartnerProfitRuleTypeEnum {

    /*** 操作类型:1:自买省 */
    SELF_PURCHASE_SAVE(1, "自买省"),
    /*** 操作类型:2:分享赚 */
    SHARE_PROFIT(2, "分享赚"),
    ;

    private int id;
    private String name;
}
