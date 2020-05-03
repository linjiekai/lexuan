package com.zhuanbo.admin.api.dto.index;

public class TopicTypeDTO {
    private static final long serialVersionUID = 1L;
    private Integer type;
    private String name;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "TopicTypeDTO{" +
                "type=" + type +
                ", name='" + name + '\'' +
                '}';
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
