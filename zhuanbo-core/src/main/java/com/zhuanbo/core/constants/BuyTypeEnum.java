package com.zhuanbo.core.constants;

/**
 * @author Administrator
 */
public enum BuyTypeEnum {

    /*** 普通购买 */
    BUY_TYPE_0(0),
    /*** 分享购买 */
    BUY_TYPE_1(1),
    /*** 赠品 */
    BUY_TYPE_2(2),
    /*** 积分 */
    BUY_TYPE_3(3),
    ;

    private Integer value;

    BuyTypeEnum(Integer value) {
        this.value = value;
    }

    public Integer value() {
        return this.value;
    }
}

