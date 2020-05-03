package com.zhuanbo.core.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminRealNameDTO extends AdminBaseRequestDTO {

    private Long id;

    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 姓名
     */
    private String name;

    /**
     * 身份证号码
     */
    private String cardNo;

    /**
     * 身份证正面
     */
    private String imgFront;

    /**
     * 身份证反面
     */
    private String imgBack;


    /**
     * 终端系统 IOS、ANDROI、H5、WX-APPLET
     */
    private String sysCnl;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 关联订单
     */
    private String orderNo;
}
