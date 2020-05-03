package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 提现方式
 *
 * @date 2019/11/6 10:20
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum WithDrawTypeEnum {

    /**
     * 1:ALIPAY:支付宝
     */
    ALIPAY(1, "ALIPAY", "支付宝"),
    /**
     * 2:WEIXIN:微信
     */
    WEIXIN(2, "WEIXIN", "微信"),
    /**
     * 3:BANK_CARD:银行卡
     */
    BANK_CARD(3, "BANK_CARD", "银行卡"),
    /**
     * 4:MPPAY:名品猫
     */
    MPPAY(4, "MPPAY", "名品猫"),
    ;

    /**
     * 根据[银行编码]获取[提现方式]
     *
     * @param bankCode
     * @return
     */
    public static WithDrawTypeEnum getByBankCode(String bankCode) {
        WithDrawTypeEnum[] drawTypeEnums = WithDrawTypeEnum.values();
        for (int i = 0; i < drawTypeEnums.length; i++) {
            WithDrawTypeEnum drawTypeEnum = drawTypeEnums[i];
            if (drawTypeEnum.code.equals(bankCode)) {
                return drawTypeEnum;
            }
        }
        return WithDrawTypeEnum.BANK_CARD;
    }

    private int id;
    private String code;
    private String name;
}
