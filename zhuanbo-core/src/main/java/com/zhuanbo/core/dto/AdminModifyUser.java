package com.zhuanbo.core.dto;

import lombok.Data;

@Data
public class AdminModifyUser {
    private Long userId;
    private String oldMobile;
    private String newMobile;
}
