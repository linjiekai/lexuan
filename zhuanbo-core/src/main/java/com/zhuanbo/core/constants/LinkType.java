package com.zhuanbo.core.constants;

public enum LinkType {
	TYPE_GOOD(0, "商品"),
	TYPE_ACTIVE(1, "活动"),
	TYPE_DYNAMIC(2, "动态"),
	UNKNOWN(-1, "未知"),;

	private Integer id;

	private String name;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private LinkType(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	public static LinkType parse (String id) {
		for (LinkType linkType : LinkType.values()) {
			if (linkType.getId().equals(id)) {
				return linkType;
			}
		}
		return LinkType.UNKNOWN;
	}
}
