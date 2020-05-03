package com.zhuanbo.core.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransDetailListVO {

    private Integer ptLeve;
    private Integer orderType;
    private BigDecimal price;
    private Integer buyNum;
    private Integer operateType;
    private Integer purchType;
    private String transDate;
    private String transTime;
    private String content;
    private String headImgUrl;
    private String nickname;
    private LocalDateTime addTime;
    private LocalDateTime updateTime;
    private Integer ptLevel;
    private Long userId;
}
