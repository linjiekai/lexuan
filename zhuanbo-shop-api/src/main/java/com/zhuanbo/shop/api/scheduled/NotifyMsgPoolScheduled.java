package com.zhuanbo.shop.api.scheduled;

import com.zhuanbo.service.service.IHeadImgService;
import com.zhuanbo.service.service.INotifyMsgPoolService;
import com.zhuanbo.service.service.IUserVirtualService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单查询补单
 *
 */
@Component
@Slf4j
public class NotifyMsgPoolScheduled {

	// 用来标识定时器状态 1：开启 0：关闭
	public static Integer SCHEDULED_STATUS = 0;

	private final String MSG = "加入了";

	@Autowired
	private INotifyMsgPoolService iNotifyMsgPoolService;
	@Autowired
	private IUserVirtualService iUserVirtualService;
	@Autowired
	private IHeadImgService iHeadImgService;


	/**
	 * 创建虚拟广告池数据（30分钟一次：10-24小时内：20条）
	 */
	@Scheduled(cron = "${scheduled.notify-pool-virtual}")
    public void notifyPoolMsg() {

		/*if (Constants.SCHEDULER_SWITCH == 0) {
			SCHEDULED_STATUS = 0;
			return;
		}
		SCHEDULED_STATUS = 1;
		try {
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime localDateTime10 = now.withHour(10).withMinute(0).withSecond(0);
			LocalDateTime localDateTime24 = now.plusDays(1).withHour(0).withMinute(0).withSecond(0);
			if (localDateTime10.isBefore(now) && now.isBefore(localDateTime24)) {
				List<UserVirtual> virtualList = iUserVirtualService.list(new QueryWrapper<>());
				if (virtualList.isEmpty()) {
					return;
				}

				List<HeadImg> headImgList = iHeadImgService.list(new QueryWrapper<>());
				int end = virtualList.size();
				int num = 0;
				NotifyMsgPool notifyMsgPool;
				PtLevelType ptLevel = null;
				while (num < 20) {
					notifyMsgPool = new NotifyMsgPool();
					UserVirtual uv = virtualList.get(ThreadLocalRandom.current().nextInt(0, end));
					notifyMsgPool.setUserId(uv.getId());
					notifyMsgPool.setNickname(uv.getNickname());
					
					ptLevel = PtLevelType.parse(uv.getPtLevel());
					
					if (headImgList.size() > 0) {
						HeadImg headImg = headImgList.get(ThreadLocalRandom.current().nextInt(0, headImgList.size()));
						notifyMsgPool.setHeadImgUrl(headImg.getHeadImgUrl());
					}
					notifyMsgPool.setPlatform(ConstantsEnum.PLATFORM_ZBMALL.stringValue());
					notifyMsgPool.setMsgType(2);
					notifyMsgPool.setStatus(1);
					notifyMsgPool.setContent(MSG + ptLevel.getName());
					notifyMsgPool.setMsgDate(DateUtil.toyyyy_MM_dd(now));
					notifyMsgPool.setMsgTime(DateUtil.toHH_mm_ss(now));
					notifyMsgPool.setAddTime(now);
					notifyMsgPool.setUpdateTime(now);
					iNotifyMsgPoolService.save(notifyMsgPool);
					num++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("定时器：NotifyMsgPoolScheduled：失败：{}", e);
		}
		SCHEDULED_STATUS = 0;*/
	}
}
