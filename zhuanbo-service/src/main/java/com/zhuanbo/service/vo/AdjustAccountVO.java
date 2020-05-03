package com.zhuanbo.service.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdjustAccountVO {

    private Long id;
    private Long userId;
    private String orderNo;
    private Integer adjustCategory;
    private Integer adjustType;
    private Integer operateType;
    private Long adjustUserId;
    private String reason;
    private String remark;
    private Long operatorId;
    private String operator;
    private BigDecimal price;
    private String nickname;
    private String adjustUserNickname;
    private Integer orderType;
    private LocalDateTime addTime;
    private LocalDateTime updateTime;
}
