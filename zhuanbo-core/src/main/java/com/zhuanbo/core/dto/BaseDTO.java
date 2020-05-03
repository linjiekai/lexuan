package com.zhuanbo.core.dto;

import lombok.Data;

@Data
public class BaseDTO {
    private String mercId;
    private String platform;
    private String sysCnl;
    private String timestamp;
    private Integer page = 1;
    private Integer limit = 10;
}
