package com.zhuanbo.shop.api.dto.req;

import lombok.Data;

@Data
public class UserDTO extends BaseParamsDTO{

	private Integer ptLevel;

	/**
	 *  邀请卡版本
	 */
	private Integer inviteCardVersion = 1;

	/**
	 * 签名图片地址
	 */
	private String signImgUrl;

}
