package com.zhuanbo.core.constants;

/**
 * 已购类型枚举
 */
public enum PurchasedTypeEnum {

	// 用户等级
	GIFT3(1),
	GIFT6(2),
	GIFT9(4)
	;
	private Integer value;

	PurchasedTypeEnum(Integer value){
		this.value = value;
	}

	public Integer Integer(){
		return Integer.valueOf(value.toString());
	}


	public static PurchasedTypeEnum getOne(Object value, String type){
		for (PurchasedTypeEnum userEnum : values()) {
			if (userEnum.value.equals(value)) {
				return userEnum;
			}
		}
		return null;
	}
}
