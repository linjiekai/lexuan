package com.zhuanbo.service.vo;

import com.zhuanbo.core.entity.User;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class NotifyPushPackVO {
    private User user;
    private String platform;
    private String title;
    private String content;
    private Map map;
}
