package com.zhuanbo.core.dto;

import lombok.Data;

@Data
public class WxUserDTO {

    private Long id;
    private String mobile;
    private String nickname;
    private String wxnickname;
    private String imgUrl;
    private String bindType;

}
