package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum IncomeContentType {

	ORDER_BUY(1, "购买商品"),
	ORDER_PURCH(2, "进货商品"),
    ORDER_REFUND(3, "订单退货"),
    CLOUD_STOCK_REFUND(4, "云仓退货"),
    WITHDR(5, "提现"),
    ONLINE(6, "购买商品"),
    ;

    private int id;
    private String name;

    /**
     * 根据id获取奖励类型
     *
     * @param id
     * @return
     */
    public static IncomeContentType parse(int id) {
        for (IncomeContentType type : IncomeContentType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }

    public static String toName(int id) {
        for (IncomeContentType type : IncomeContentType.values()) {
            if (id == type.id) {
                return type.name;
            }
        }
        return "";
    }
}
