package com.zhuanbo.admin.api.dto.order;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderParamsDTO {

    private Integer userId;
    private String orderNo;
    private String orderStatus;
    private Long goodsId;
    private String payNo;
    private String supplierCode;// 供应商编号
    private String mobile;
    private String startDate;
    private String endDate;
    private Long inviteUserId;// 上级id
}
