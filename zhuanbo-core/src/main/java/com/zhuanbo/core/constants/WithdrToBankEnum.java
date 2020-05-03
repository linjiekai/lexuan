package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 是否开放[提现到银行卡] 0:关闭，1:开放
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum WithdrToBankEnum {

    /**
     * 是否开放[提现到银行卡]:0:关闭
     */
    OFF(0, "关闭"),
    /**
     * 是否开放[提现到银行卡]:1:开放
     */
    ON(1, "开放"),
    ;

    private int id;
    private String name;

}
