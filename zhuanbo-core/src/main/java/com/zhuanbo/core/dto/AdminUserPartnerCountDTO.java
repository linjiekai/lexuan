package com.zhuanbo.core.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUserPartnerCountDTO {

   private String title;
    private int ptLevel;
    private int count;
}
