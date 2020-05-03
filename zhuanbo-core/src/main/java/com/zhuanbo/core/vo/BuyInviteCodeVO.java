package com.zhuanbo.core.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BuyInviteCodeVO implements Serializable {
    private static final long serialVersionUID = 4702274010974523293L;
    private Long id;
    private Integer ptLevel;
    private String buyInviteCode;
}
