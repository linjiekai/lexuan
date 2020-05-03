package com.zhuanbo.service.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UserIncomeVO {

    //累计收益
    private BigDecimal totalIncome;
    //在途中收益总数
    private BigDecimal totalUavaIncome;
    //今日在途收益
    private BigDecimal todayUavaIncome;
    //今日可提收益
    private BigDecimal todayWithdrawIncome;
    //今日收益
    private BigDecimal todayTotalIncome;
    //累计销售收益
    private BigDecimal shareIncome;

    //累计销售
    private BigDecimal totalConsume;
    //今日销售
    private BigDecimal todayTotalConsume;

    //总订购数
    private Integer totalBuy;
    //今日订购数
    private Integer todayTotalBuy;

    //总客户数
    private Integer totalTeam;
    //今日客户数
    private Integer todayTotalTeam;


    /**
     * 可用提现余额
     */
    private BigDecimal acBal;

    /**
     * 不可用金额
     */
    private BigDecimal uavaBal;
    /**
     * 已提现
     */
    private BigDecimal withdrBal;


    private List<teamUser> teamList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class teamUser {
        private Integer id;
        private Integer inviteUpId;
        private Integer level;
    }
}
