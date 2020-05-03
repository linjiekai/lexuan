package com.zhuanbo.shop.api.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class NotifyMsgDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String logo;
    private String nickname;
    private String title;
    private Integer msgFlag;
    private Integer msgType;
    private Integer readFlag;
    private String content;
    private String howLongTime;
    private String msgFlagTitle;
    private LocalDateTime addTime;
}
