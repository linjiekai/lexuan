package com.zhuanbo.service.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransCodeVO {

    private String mode;
    private Long id;
    private String videoId;
    private LocalDateTime time;// mq发送时间,和update_time一样
    private Integer result;// 0: 转码处理完成， -1：异常，1：发了起转码，但还不能获取url
}
