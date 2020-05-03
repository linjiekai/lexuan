package com.zhuanbo.core.dto;

import lombok.Data;

@Data
public class MqUserLevelDTO {
    private Long userId;
    private Integer level;
    private Long inviteUpUserId;
    private Integer status;
}
