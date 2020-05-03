package com.zhuanbo.core.dto;

import lombok.Data;

@Data
public class GiftDTO {
    private Long userId;
    private Integer page = 0;
    private Integer limit = 10;
}
