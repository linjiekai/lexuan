package com.zhuanbo.admin.api.dto.brand;

import lombok.Data;

@Data
public class GoodDTO {
    //商品id
    private Integer id;
    //商品名称
    private String name;

    private String alias;

    private String image;

    public GoodDTO(){}

    public GoodDTO(Integer id, String name) {
        this.id=id;
        this.name=name;
    }
}
