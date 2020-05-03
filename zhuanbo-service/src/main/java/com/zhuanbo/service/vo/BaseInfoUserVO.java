package com.zhuanbo.service.vo;

import com.zhuanbo.core.entity.User;
import lombok.Data;

@Data
public class BaseInfoUserVO extends User {
    private Long inviteUserId;// 邀请人
    private String authNo;
}
