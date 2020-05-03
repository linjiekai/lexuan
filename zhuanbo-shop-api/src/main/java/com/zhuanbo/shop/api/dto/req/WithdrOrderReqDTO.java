package com.zhuanbo.shop.api.dto.req;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrOrderReqDTO extends BaseParamsDTO{
    //银行编号
    private String bankCode;
    //换取access_token的code
    private String code;
    //银行卡号
    private String bankCardNo;
    private String clientIp = "127.0.0.1";
    private BigDecimal price;
    private String tradeType;
    private String bankCardType = "08";
    private String agrNo;
    private String mobile;
    private String bankCardName;
    private String bankNo;
    private String bankProv;
    private String bankCity;
    private String cardNo;
    private Integer cardType;
    private String smsCode;
    private String smsOrderNo;
    private String bankCardImgFront;
    private String imgFront;
    private String imgBack;
}
