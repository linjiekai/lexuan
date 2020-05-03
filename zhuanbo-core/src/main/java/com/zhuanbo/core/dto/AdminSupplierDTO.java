package com.zhuanbo.core.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminSupplierDTO  extends AdminBaseRequestDTO {

    private Long id;

    private String name;

    /**
     * 订单对接方式。0：自动、1：手动
     */
    private Integer orderConnectType;


    private LocalDateTime addTime;

    private LocalDateTime updateTime;
}
