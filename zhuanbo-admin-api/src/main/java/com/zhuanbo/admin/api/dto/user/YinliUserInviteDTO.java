package com.zhuanbo.admin.api.dto.user;

import lombok.Data;

@Data
public class YinliUserInviteDTO {

	private YinliUserDTO user;
	private YinliUserDTO pidUser;//邀请人
	private String inviteMonth;

}
