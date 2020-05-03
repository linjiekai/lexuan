package com.zhuanbo.core.dto;

import lombok.Data;

@Data
public class WithdrawOrderAuditDTO {

    private String orderNo;

    /**
     * 订单流水状态 A:审核中, W:待提现, S:提现成功, F:提现失败, R:审核拒绝
     */
    private String orderStatus;

    private String auditMsg;

    public WithdrawOrderAuditDTO() {
    }

    public WithdrawOrderAuditDTO(String orderNo) {
        this.orderNo = orderNo;
    }

    @Override
    public String toString() {
        return "WithdrawOrderAuditDTO{" +
                "orderNo='" + orderNo + '\'' +
                ", orderStatus='" + orderStatus + '\'' +
                ", auditMsg='" + auditMsg + '\'' +
                '}';
    }
}
