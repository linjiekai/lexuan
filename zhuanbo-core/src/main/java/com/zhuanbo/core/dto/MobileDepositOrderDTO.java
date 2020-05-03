package com.zhuanbo.core.dto;

import lombok.Data;


@Data
public class MobileDepositOrderDTO extends MobileBaseParamsDTO{

    /**
     * 商城会员ID
     */
    private Long userId;
    /**
     * 业务类型
     */
    private String busiType;
    /**
     * 交易类型
     */
    private String tradeCode;
    /**
     * 订单状态 [W:待充值, S:充值成功, BW:充值确认, F:充值失败]
     */
    private String orderStatus;
    /**
     * 充值订单号
     */
    private String depositNo;

}
