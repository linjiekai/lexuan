package com.zhuanbo.shop.api.dto.req;

public class AdParamsDTO extends BaseParamsDTO {

    private static final long serialVersionUID = 1L;
    private Integer position;

    private String platform;


    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getPlatform() { return platform; }

    public void setPlatform(String platform) { this.platform = platform; }
}
