package com.zhuanbo.core.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商品获取dto
 * @author Administrator
 */
@Data
public class InvestorsCheckUserDTO implements Serializable {

	/**
	 * 邀请码
	 */
	private String inviteCode;
	/**
	 * 区号
	 */
	private String areaCode;
	/**
	 * 手机号码
	 */
	private String mobile;
	/**
	 * 用户等级列表
	 */
	private List level;

}
