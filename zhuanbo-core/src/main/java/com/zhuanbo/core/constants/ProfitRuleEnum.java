package com.zhuanbo.core.constants;

/**
 * 用户枚举
 */
public enum ProfitRuleEnum {

	/**399*/
	MODE_TYPE_0(0,"modeType"),
	/**600*/
	MODE_TYPE_1(1,"modeType"),
	/**9980*/
	MODE_TYPE_2(2,"modeType")
	;
	private Object value;
	private String type;

	ProfitRuleEnum(Object value, String type){
		this.value = value;
		this.type = type;
	}

	public Integer Integer(){
		return Integer.valueOf(value.toString());
	}

	public String String(){
		return value.toString();
	}

	public String Type() {
		return this.type;
	}

	public static ProfitRuleEnum getOne(Object value, String type){
		for (ProfitRuleEnum userEnum : values()) {
			if (userEnum.value.equals(value) && userEnum.type.equals(type)) {
				return userEnum;
			}
		}
		return null;
	}
}
