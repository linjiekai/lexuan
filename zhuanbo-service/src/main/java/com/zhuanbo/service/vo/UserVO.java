package com.zhuanbo.service.vo;

import com.zhuanbo.core.entity.User;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserVO extends User {
    /**
     * 邀请人ID
     */
    private Long pid;
    private Integer totalTeam;// 团队人数
    /**
     * 真实名字
     */
    private String realName;
    /**
     * 身份证正面
     */
    private String cardImgFront;

    /**
     * 身份证反面
     */
    private String cardImgBack;

    private BigDecimal totalSale=new BigDecimal(0);// 累计销售
    private BigDecimal totalIncome=new BigDecimal(0);// 累计收益
    private BigDecimal totalUavaIncome=new BigDecimal(0);// 在途收益
    private BigDecimal withdrawIncome=new BigDecimal(0);// 可提收益
    private BigDecimal totalBuy=new BigDecimal(0);//累计订购量
    private BigDecimal withdrawAlready=new BigDecimal(0);//已提现


}
