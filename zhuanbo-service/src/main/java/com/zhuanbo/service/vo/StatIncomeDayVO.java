package com.zhuanbo.service.vo;

import com.zhuanbo.core.annotation.ExcelAnnotation;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 收益日统计报表
 * </p>
 *
 * @author rome
 * @since 2019-06-20
 */
@Data
public class StatIncomeDayVO implements Serializable {

    @ExcelAnnotation(id = 1,name = "日期",width = 5000)
    private String statDate;
    @ExcelAnnotation(id = 2,name = "名品猫销售额",width = 5000)
    private BigDecimal vsOrderPrice;
    @ExcelAnnotation(id = 3,name = "订单量",width = 5000)
    private Integer vsOrderCount;
    @ExcelAnnotation(id = 4,name = "9980充值费",width = 5000)
    private BigDecimal vsDepositPrice;
    @ExcelAnnotation(id = 5,name = "9980订单量",width = 5000)
    private Integer vsDepositCount;
    @ExcelAnnotation(id = 6,name = "基础课时",width = 5000)
    private BigDecimal vsTrainIncomeBase;
    @ExcelAnnotation(id = 7,name = "名品课时费",width = 5000)
    private BigDecimal vsTrainIncomeMp;
    @ExcelAnnotation(id = 8,name = "合伙人课时费",width = 5000)
    private BigDecimal vsTrainIncomePartner;
    @ExcelAnnotation(id = 9,name = "自买省钱",width = 5000)
    private BigDecimal vsEconIncome;
    @ExcelAnnotation(id = 10,name = "分享赚钱",width = 5000)
    private BigDecimal vsShareIncome;
    @ExcelAnnotation(id = 11,name = "销售提成",width = 5000)
    private BigDecimal vsSaleIncome;
    @ExcelAnnotation(id = 12,name = "累计收益",width = 5000)
    private BigDecimal vsTotalIncome;
    @ExcelAnnotation(id = 13,name = "提现金额",width = 5000)
    private BigDecimal vsWithdrIncome;
    @ExcelAnnotation(id = 14,name = "新增邀请关系",width = 5000)
    private Integer vsUserCount;
}
