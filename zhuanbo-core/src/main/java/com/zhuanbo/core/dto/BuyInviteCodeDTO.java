package com.zhuanbo.core.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class BuyInviteCodeDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Integer ptLevel;
    private String buyInviteCode;
    private Long userId;
}
