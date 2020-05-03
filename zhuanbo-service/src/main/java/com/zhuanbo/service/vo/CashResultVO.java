package com.zhuanbo.service.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付请求接口调用返回的封装类
 */
@Data
public class CashResultVO {
    private String code;// 成功：10000
    private String orderStatus; // 订单状态 预登记A,成功S,失败F,等待支付W,全额退款RF,部分退款RP
    private String mercId;
    private String bankCode;
    private String orderNo;
    private String payTime;
    private String userId;
    private String payNo;
    private String orderTime;
    private BigDecimal price;
    private String appId;
    private String orderDate;
    private String tradeType;
    private String payDate;
}
