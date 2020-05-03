package com.zhuanbo.core.vo;

import com.zhuanbo.core.annotation.ExcelAnnotation;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 提现信息导出
 */
@Data
public class WithdrOrderExportVO implements Serializable {

    @ExcelAnnotation(id=1,name={"用户id"},width = 5000)
    private Long userId;
    @ExcelAnnotation(id=2,name={"提现订单编号"},width = 5000)
    private String orderNo;
    @ExcelAnnotation(id=3,name={"订单状态"},width = 5000)
    private String orderStatusName;
    private String orderStatus;
    @ExcelAnnotation(id=4,name={"提现金额"},width = 5000)
    private BigDecimal price;
    @ExcelAnnotation(id=5,name={"银行卡号"},width = 5000)
    private String bankCardNoText;
    private String bankCardNo;
    @ExcelAnnotation(id=6,name={"账户名"},width = 5000)
    private String bankCardName;
    @ExcelAnnotation(id=7,name={"银行"},width = 5000)
    private String bankCodeName;
    private String bankCode;
    @ExcelAnnotation(id=8,name={"团队名"},width = 5000)
    private String teamName;
    @ExcelAnnotation(id=9,name={"手机号"},width = 5000)
    private String mobile;
    @ExcelAnnotation(id=10,name={" 收益总数"},width = 5000)
    private BigDecimal totalIncome;
    @ExcelAnnotation(id=11,name={"在途收益"},width = 5000)
    private BigDecimal totalUavaIncome;
    @ExcelAnnotation(id=12,name={"可提收益"},width = 5000)
    private BigDecimal acBal;
    @ExcelAnnotation(id=13,name={"已提现金额"},width = 5000)
    private BigDecimal withdrBal;
    @ExcelAnnotation(id=14,name={"证件号"},width = 5000)
    private String cardNoText;
    private String cardNo;
    @ExcelAnnotation(id=15,name={"证件类型"},width = 5000)
    private String cardTypeName;
    private Integer cardType;
    @ExcelAnnotation(id=16,name={"提现日期"},width = 5000)
    private String bankWithdrDate;
    @ExcelAnnotation(id=17,name={"提现时间"},width = 5000)
    private String bankWithdrTime;

}
