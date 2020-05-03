package com.zhuanbo.core.constants;

public enum ModeType {
	BASE(0, "基础"),
	MP(1, "名品"),
	PARTNER(2, "合伙人"),
	;
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

	private ModeType(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	public static ModeType parse (String id) {
		for (ModeType modeType : ModeType.values()) {
			if (modeType.getId().equals(id)) {
				return modeType;
			}
		}
		return null;
	}
}
