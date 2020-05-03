package com.zhuanbo.shop.api.scheduled;

import com.zhuanbo.service.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InviteCodeScheduled {


	@Autowired
	private IUserService iUserService;

	/**
	 * 邀请码生成
	 */
	@Scheduled(cron = "0 0/30 * * * ?")
	public void inviteCodeGenerate() {
		iUserService.checkInviteCodeNumber();
	}
}
