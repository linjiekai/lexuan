package com.zhuanbo.core.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class GiftAdjustDTO implements Serializable {

    private String authNo;
    private Integer operateType;//操作类型 [1:增加收益, 2:减少收益]
    private Integer baseNum;//基础礼包
    private Integer giftNum;//赠送礼包

    @Override
    public String toString() {
        return "GiftAdjustDTO{" +
                "authNo='" + authNo + '\'' +
                ", operateType=" + operateType +
                ", baseNum=" + baseNum +
                ", giftNum=" + giftNum +
                '}';
    }
}
