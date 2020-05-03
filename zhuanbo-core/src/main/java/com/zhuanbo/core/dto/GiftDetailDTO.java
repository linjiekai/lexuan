package com.zhuanbo.core.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GiftDetailDTO extends AdminBaseRequestDTO{

    public Long id;
    private Long userId;
    private String userName;
    private Long fromUserId;
    private String orderNo;
    private Integer operateType;//操作类型 [1:增加收益, 2:减少收益]
    private Integer purchType;//进货类型 1：购买自用 2：进货云仓 3：提货 4：线下发货
    private Integer changeType;//指标类型 [1:基础礼包, 2:赠送礼包]

    private Integer ptLevel;
    private Integer operateGift;
    private Integer giftType;// 礼包类型 0:普通(非购买得12个礼包) 1:基础, 2:赠送
    private LocalDateTime updateTime;
    private String mobile;
}
