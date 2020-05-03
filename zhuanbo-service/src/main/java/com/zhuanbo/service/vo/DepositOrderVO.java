package com.zhuanbo.service.vo;

import com.zhuanbo.core.annotation.ExcelAnnotation;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositOrderVO  {
   @ExcelAnnotation(id=1,name={"支付流水"},width = 5000)
   private String payNo;
   @ExcelAnnotation(id=2,name={"订单号"},width = 5000)
   private String depositNo;
   @ExcelAnnotation(id=3,name={"用户ID"},width = 5000)
   private Long userId;
   @ExcelAnnotation(id=4,name={"用户昵称"},width = 5000)
   private String nickname;
   @ExcelAnnotation(id=5,name={"金额"},width = 5000)
   private BigDecimal price;
   @ExcelAnnotation(id=6,name={"支付状态"},width = 5000)
   private String orderStatus;
   @ExcelAnnotation(id=7,name={"支付方式"},width = 5000)
   private String bankCode;
   @ExcelAnnotation(id=8,name={"支付时间"},width = 5000)
   private String payDateTime;

   private String payDate;

   private String payTime;
}
