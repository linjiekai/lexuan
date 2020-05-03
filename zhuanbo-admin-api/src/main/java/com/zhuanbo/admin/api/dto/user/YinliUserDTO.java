package com.zhuanbo.admin.api.dto.user;

import lombok.Data;

@Data
public class YinliUserDTO {

	private String mobile;
	private String password;
	private String headImgUrl;
	private String name;
//	private String inviteCode;
	private Integer ptLevel;
	private Integer status;
	private String authNo;

}
