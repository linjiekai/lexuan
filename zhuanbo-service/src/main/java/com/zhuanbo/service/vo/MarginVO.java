package com.zhuanbo.service.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MarginVO {

    private Integer id;
    private String orderNo;//流水号
    private Integer userId;
    private String userName;
    private String authNo;// 授权码
    private String changeType;// 变更类型 0：充值 1：退款 2：调账 3：奖励 4：提现
    private Integer operateType;// 操作类型 1：增 2：减
    private String price;
    private String bankCode;// 银行编号：支付宝：ALIPAY 微信：WEIXIN 名品支付：MPPAY
    private LocalDateTime addTime;
    private String operator;
}
