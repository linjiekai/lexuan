package com.zhuanbo.core.dto;

import lombok.Data;


@Data
public class AdminDepositOrderDTO  extends AdminBaseRequestDTO{

	private String payNo;//支付流水号
    private String userId;//商城会员ID
    private String startPayDate;//查询时间端开始日期
    private String endPayDate;//查询时间段结束日期
    private String name;
    private String mobile;
}
