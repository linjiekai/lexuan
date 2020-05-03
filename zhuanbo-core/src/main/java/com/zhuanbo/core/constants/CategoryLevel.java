package com.zhuanbo.core.constants;

public enum CategoryLevel {
	LEVEL1(1, "一级分类"),
	LEVEL2(2, "二级分类"),
	LEVEL3(3, "三级分类");

	private int id;

	private String name;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private CategoryLevel(int id, String name) {
		this.id = id;
		this.name = name;
	}

	
}
