package com.zhuanbo.core.constants;

/**
 * @author: Jiekai Lin
 * @Description(描述) :	供应商
 * @date: 2019/8/13 21:35
 */
public enum SupplierType {

    Supplier_0(0, "幸福狐狸"),
    Supplier_1(1, "候鸟");

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

    private SupplierType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static SupplierType paras(int id) {
        for (SupplierType type : SupplierType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }
}
