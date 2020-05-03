package com.zhuanbo.admin.api.dto.user;

import lombok.Data;

@Data
public class UserDTO {

    private Long id;
    private String mobile;
    private Integer ptLevel;
    private Long pid;
    private Integer inviteNumber;
    private Integer listType;
    /**
     * 证件号
     */
    private String cardNo;
    private String nickname;
}
