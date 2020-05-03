package com.zhuanbo.core.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotifyPushMQVO {
    
    private Long userId;
    private String nickname;
    private String platform;
    private String title;
    private String content;
    private Integer msgFlag;
    private Map extra;
}
