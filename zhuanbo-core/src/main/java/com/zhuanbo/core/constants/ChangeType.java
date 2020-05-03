package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author: Jiekai Lin
 * @Description(描述): 变更类型
 * @date: 2020/3/19 17:05
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ChangeType {

	/*** 变更类型:0:充值 */
    DEPOSIT(0, "充值"),
	/*** 变更类型:1:退款 */
    REFUND(1, "退款"),
	/*** 变更类型:2:调账 */
    ADJUST(2, "调账"),
	/*** 变更类型:3:奖励 */
    INCOME(3, "奖励"),
	/*** 变更类型:4:提现 */
    WITHDR(4, "提现"),
    ;

    private int id;
    private String name;

    public static ChangeType parse(int id) {
        for (ChangeType type : ChangeType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }
}
