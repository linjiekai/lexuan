package com.zhuanbo.external.service.dto;

import lombok.Data;

@Data
public class AppIdKeyDTO {

    private String methodType = "GetAppId";
    private String mercId;
    private String platform = "ZBMALL";
    private String requestId;
    private String tradeType = "APP";
    private Integer operType = 1;
    private String bankCode = "WEIXIN";
    private String sysCnl ="WEB";
    private String clientIp;
    private String source;
    private String code;
    private String url;
}
