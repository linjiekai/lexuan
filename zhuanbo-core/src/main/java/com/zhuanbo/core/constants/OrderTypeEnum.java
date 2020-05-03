package com.zhuanbo.core.constants;

public enum OrderTypeEnum {

	BUY_ORDER_TYPE_0(0, "商品"),
	BUY_ORDER_TYPE_1(1, "VIP"),
	BUY_ORDER_TYPE_2(2, "店长"),
	BUY_ORDER_TYPE_3(3, "总监"),
	BUY_ORDER_TYPE_4(4, "合伙人"),
	BUY_ORDER_TYPE_5(5, "联创"),
	;
	private Integer value;
	private String name;
	
	OrderTypeEnum(Integer value, String name) {
		this.value = value;
		this.name = name;
	}

	public Integer value () {
		return this.value;
	}
	
	public static OrderTypeEnum parse (Integer value) {
		for (OrderTypeEnum type : OrderTypeEnum.values()) {
			if (type.getValue().equals(value)) {
				return type;
			}
		}
		
		return null;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
