package com.zhuanbo.core.constants;

/**
 * 用户枚举
 */
public enum UserEnum {

	// 用户等级
	PT_LEVEL(-99999,"ptLevel"),
	PT_LEVEL_0(0, "ptLevel"),// 普
	PT_LEVEL_1(1, "ptLevel"),// 达
	PT_LEVEL_2(2, "ptLevel"),// 体
	PT_LEVEL_3(3, "ptLevel"),// 司
	PT_LEVEL_4(4, "ptLevel"),// 司合伙人
	PT_LEVEL_5(5, "ptLevel"),// M司令(高级合伙人)
	PT_LEVEL_6(6, "ptLevel"),// M司令(总监)
	PT_LEVEL_7(7, "ptLevel"),// M司令(高级总监)
	PT_LEVEL_8(8, "ptLevel"),// M司令(代言人)
	PT_LEVEL_9(9, "ptLevel"),// M司令(高级代言人)
	;
	private Object value;
	private String type;

	UserEnum(Object value, String type){
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

	public static UserEnum getOne(Object value, String type){
		for (UserEnum userEnum : values()) {
			if (userEnum.value.equals(value) && userEnum.type.equals(type)) {
				return userEnum;
			}
		}
		return null;
	}
}
