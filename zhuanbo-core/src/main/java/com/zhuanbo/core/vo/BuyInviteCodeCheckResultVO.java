package com.zhuanbo.core.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuyInviteCodeCheckResultVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer orderType;// 返回最大的
    private BigDecimal price;
    private String msg;
    private List<Integer> orderTypeList;
}
