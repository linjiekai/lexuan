package com.zhuanbo.core.constants;

/**
 * @author: Jiekai Lin
 * @Description(描述)  :	贸易类型
 * @date: 2019/8/13 21:35
 */
public enum TraceType {

	TraceType_0(0, "一般贸易"),
	TraceType_1(1, "保税/香港直邮"),
	TraceType_2(2, "海外直邮（非韩澳）"),
	TraceType_3(3, "海外直邮（韩澳）");

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

	private TraceType(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public static TraceType paras (int id) {
		for (TraceType type : TraceType.values()) {
			if (type.getId() == id) {
				return type;
			}
		}
		return null;
	}
}
