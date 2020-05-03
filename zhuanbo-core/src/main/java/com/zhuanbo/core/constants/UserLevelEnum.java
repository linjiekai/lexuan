package com.zhuanbo.core.constants;

/**
 * 用户等级枚举
 */
public enum UserLevelEnum {

	PT_LEVEL_0(0, "普通用户"),
	PT_LEVEL_1(1, "VIP"),
	PT_LEVEL_2(2, "店长"),
	PT_LEVEL_3(3, "总监"),
	PT_LEVEL_4(4, "合伙人"),
	PT_LEVEL_5(5, "联创"),
	PT_LEVEL_6(6, "分公司"),
	PT_LEVEL_7(7, "总公司");

	private Integer value;
	private String desc;

	UserLevelEnum(Integer value, String desc){
		this.value = value;
		this.desc = desc;
	}

	public Integer value(){
		return this.value;
	}

	public String desc(){
		return this.desc;
	}

	public static String desc(Integer value){
		for (UserLevelEnum userEnum : values()) {
			if (userEnum.value.equals(value)) {
				return userEnum.desc;
			}
		}
		return null;
	}

	public static UserLevelEnum getOne(Object value, String desc){
		for (UserLevelEnum userEnum : values()) {
			if (userEnum.value.equals(value) && userEnum.desc.equals(desc)) {
				return userEnum;
			}
		}
		return null;
	}
}
