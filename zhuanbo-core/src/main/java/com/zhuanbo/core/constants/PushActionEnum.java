package com.zhuanbo.core.constants;

public enum PushActionEnum {

	UPGRADE("upgrade"),// 升级
	REGISTER("register"),// 注册
	ORDER_PAY("orderPay");// 订单支付完成

	private String value;
	PushActionEnum(String value) {
		this.value = value;
	}

	public String value() {
		return this.value;
	}

	public static PushActionEnum getOne(String value) {
		for (PushActionEnum pushActionEnum : values()) {
			if (pushActionEnum.value.equals(value)) {
				return pushActionEnum;
			}
		}
		return null;
	}
}
