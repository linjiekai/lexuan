package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * mappay 支付平台接口
 */
@Getter
@AllArgsConstructor
public enum PayInterfaceEnum {

    /**
     * 提现银行卡列表
     */
    WITHER_BANK_LIST("/admin/card/bind/withdr/bank/list","提现银行卡列表"),
    /**
     * 快捷银行卡列表
     */
    QUICK_BANK_LIST("/admin/quick/agr/quick/bank/list","快捷银行卡列表"),
    /**
     * 取消实名
     */
    CANCEL_REALNAME("/admin/user/oper/cancel/realname","取消实名"),
    /**
     * 实名信息列表
     */
    REAL_NAME_LIST("/admin/user/real/name/details/list","实名信息列表"),
    /**
     * 根据银行code获取银行卡信息
     */
    BANK_LIST_BY_BANKCODE("/admin/bank/list/by/bankcode","根据银行code获取银行卡信息"),
    /**
     * 根据订单号获取支付信息
     */
    LAST_BIND_BY_USERIDS("/admin/card/bind/lastbind/byuserids","根据用户id批量查询绑卡信息"),
    ;

    private String id;

    private String name;

}
