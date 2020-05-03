package com.zhuanbo.core.constants;

/**
 * 用户枚举
 */
public enum IncomeDetailEnum {
	/**增加收益*/
	OPERATE_TYPE_1(1),
	/**减少收益*/
	OPERATE_TYPE_2(2),
	/**销售提成*/
	INCOME_TYPE_1(1),
	/**提现*/
	INCOME_TYPE_2(2),
	/**课时费*/
	INCOME_TYPE_3(3),
	/**名品课时费*/
	INCOME_TYPE_7(7),
	/**合伙人课时费*/
	INCOME_TYPE_8(8),
	/**自买省钱*/
	INCOME_TYPE_4(4),
	/**分享赚钱*/
	INCOME_TYPE_5(5),
	INCOME_TYPE_10(10),
	INCOME_TYPE_11(11),
	INCOME_TYPE_12(12),
	INCOME_TYPE_13(13),
	/**有效*/
	STATUS_1(1),
	/**直推*/
	PROFIT_TYPE_1(1),
	/**销售额*/
	PROFIT_TYPE_2(2),
	/**未统计*/
	STAT_TYPE_0(0),
	/**已统计*/
	STAT_TYPE_1(1),
	/**基础*/
	MODE_TYPE_0(0),
	/**名品*/
	MODE_TYPE_1(1),
	/**合伙*/
	MODE_TYPE_2(2)
	;
	private Object value;

	IncomeDetailEnum(Object value){
		this.value = value;
	}

	public Integer Integer(){
		return Integer.valueOf(value.toString());
	}

	public String String(){
		return value.toString();
	}

	public static IncomeDetailEnum getByValue(Object value){
		for (IncomeDetailEnum userEnum : values()) {
			if (userEnum.Integer().equals(value) || userEnum.String().equals(value)) {
				return userEnum;
			}
		}
		return null;
	}
}
