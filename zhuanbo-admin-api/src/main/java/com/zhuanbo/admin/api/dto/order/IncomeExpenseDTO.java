package com.zhuanbo.admin.api.dto.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class IncomeExpenseDTO {

    private String payNo;// 支付流水
    private String payTime;// 支付时间
    private Long userId;// 用户ID
    private String userName;// 用户昵称
    private BigDecimal price;// 金额
    private String payStatus;// 支付状态
    private String payType;// 支付方式
    private String payEe;// 收款账号
}
