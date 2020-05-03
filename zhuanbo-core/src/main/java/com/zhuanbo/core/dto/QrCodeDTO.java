package com.zhuanbo.core.dto;

import lombok.Data;

@Data
public class QrCodeDTO {
    private String content;
    private Integer width;
    private Integer height;
}
