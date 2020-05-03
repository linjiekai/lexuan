package com.zhuanbo.core.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MobileInviteCodeDTO {

    /**
     * 主键ID
     */
    private Long id;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 邀请码
     */
    private String inviteCode;
    /**
     * 被邀请用户id
     */
    private Long inviteId;
    /**
     * 有效状态 [0: 无效，1：有效]
     */
    private Integer validStatus;
    /**
     * 使用状态 [0:未使用, 1:已使用]
     */
    private Integer useStatus;
    /**
     * 有效期
     */
    private LocalDateTime expiryTime;

    /**
     * 有效期
     */
    private LocalDateTime inviteTime;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}
