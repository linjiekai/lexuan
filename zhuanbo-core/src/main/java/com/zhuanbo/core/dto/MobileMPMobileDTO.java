package com.zhuanbo.core.dto;

import lombok.Data;

@Data
public class MobileMPMobileDTO {
    private String code;
    private String encryptedData;
    private String iv;
    private String sessionKeyStr;
}
