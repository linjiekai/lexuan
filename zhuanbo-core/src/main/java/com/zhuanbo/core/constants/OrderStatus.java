package com.zhuanbo.core.constants;

public enum OrderStatus {
	WAIT_AUDIT("A", "待审批"),
	REFUSE("R", "已拒绝"),
	WAIT_PAY("W", "待支付"),
	WAIT_SHIP("WS", "待发货"),
	WAIT_DELIVER("WD", "待收货"), //也是已发货
	SUCCESS("S", "已完成"),
	CANCEL("C", "已取消"),
	DELETE("D", "已删除"),
	REFUND_WAIT("RW", "待退款"),
	REFUND_BANK_WAIT("BW", "待银行退款"),
	REFUND_SUCCESS("RS", "退款成功"),
	REFUND_FAIL("RF", "退款失败")
	;

	private String id;

	private String name;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private OrderStatus(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public static OrderStatus parse (String id) {
		for (OrderStatus orderStatus : OrderStatus.values()) {
			if (orderStatus.getId().equals(id)) {
				return orderStatus;
			}
		}
		
		return null;
	}
}
