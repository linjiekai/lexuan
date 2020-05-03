package com.zhuanbo.service.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeedbackVO {

    private Integer id;
    private Long userId;
    private String userName;
    private String userHeader;
    private String content;
    private String mobileModel;// 手机型号
    private String mobileSystemVersion;// 手机系统
    private String appVersion;// app版本
    private String network;// 网络环境
    private String[] images;
    private LocalDateTime addTime;
}
