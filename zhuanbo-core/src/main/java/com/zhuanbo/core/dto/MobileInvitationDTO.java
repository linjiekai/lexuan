package com.zhuanbo.core.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 展示我的邀请人
 */
@Data
public class MobileInvitationDTO extends BaseDTO implements Serializable {

    /**
     * 头像地址
     */
    private String headImgUrl;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 邀请时间
     */
    private LocalDateTime inviteTime;

}
