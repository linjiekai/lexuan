package com.zhuanbo.core.dto;


import java.math.BigDecimal;

public class AdminFinanceDTO extends AdminBaseRequestDTO{

    private  Long mercId; //子商户号
    private BigDecimal depositAmount; //充值金额
    private  Long operatorId; //充值金额

}
