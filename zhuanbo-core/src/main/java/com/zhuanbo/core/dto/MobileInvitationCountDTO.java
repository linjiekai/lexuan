package com.zhuanbo.core.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 展示我的邀请人
 */
@Data
public class MobileInvitationCountDTO implements Serializable {

    /**
     * 已邀请数量
     */
    private long invitedCount;
    /**
     * 还可以邀请数量
     */
    private long toInvitCount;

}
