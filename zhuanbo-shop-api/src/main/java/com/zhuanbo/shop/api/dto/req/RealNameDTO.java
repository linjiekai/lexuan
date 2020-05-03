package com.zhuanbo.shop.api.dto.req;

import lombok.Data;

@Data
public class RealNameDTO{
    private static final long serialVersionUID = 1L;
    private String mercId;
    private String platform;
    private String name;
    private Integer cardType;
    private String cardNo;
    private String imgFront;
    private String imgBack;
    private Integer realSource;
    private String sysCnl;
}
