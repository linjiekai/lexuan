package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author: Jiekai Lin
 * @Description(描述): 收益类型
 * @date: 2020/3/19 16:58
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum IncomeType {

    SALE_GOODS(1, "商品销售奖励"),
    SALE_SERVICE(2, "服务商销售奖励"),
    SALE_LOW(3, "下级销售扣减"),
    WITHDR(4, "提现"),
    STOCK_SPREAD(5, "进货差价奖励"),
    ;
    private int id;
    private String name;

    /**
     * 根据id获取奖励类型
     *
     * @param id
     * @return
     */
    public static IncomeType parse(int id) {
        for (IncomeType type : IncomeType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }

    /**
     * 校验type存在于枚举中
     *
     * @param type
     * @return
     */
    public static boolean validType(String type) {
        for (IncomeType incomeType : IncomeType.values()) {
            if (incomeType.toString().equals(type)) {
                return true;
            }
        }
        return false;
    }
}
