package com.zhuanbo.core.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品获取dto
 */
@Data
public class ProductDTO implements Serializable {

    /**
     * 货品ID
     */
    private Integer id;
    /**
     * 	商品表的商品ID
     */
    private Integer goodsId;
    /**
     * [-1:删除, 1:正常]
     */
    private Integer status;
    /**
     * 规格ID 的 list jso
     */
    private List<Specification> specification;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @Data
    public static class Specification implements Serializable {
        private Integer id;
        private Integer goodsId;
        private Integer attributeId;
        private String name;
        private String url;
        private Integer status;
    }

}
