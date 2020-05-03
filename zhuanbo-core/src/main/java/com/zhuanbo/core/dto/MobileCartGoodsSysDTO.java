package com.zhuanbo.core.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MobileCartGoodsSysDTO {

    private String platform = "MPMALL";
    private Integer goodsId;
    private String goodsSn;
    private Integer goodsType;
    private String goodsName;
    private Integer traceType;
    private List<OutProductDTO> productList;

    @Data
    public static class OutProductDTO{
        private Integer productId;
        private BigDecimal price;
        private BigDecimal profitPrice;
        private BigDecimal sharePrice;
        private String specifications;
        private String picUrl;
    }
}
