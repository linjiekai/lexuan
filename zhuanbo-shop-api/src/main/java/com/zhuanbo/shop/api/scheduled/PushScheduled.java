package com.zhuanbo.shop.api.scheduled;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.Push;
import com.zhuanbo.core.util.LogUtil;
import com.zhuanbo.service.service.IPushService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 推送
 */
@Component
@Slf4j
public class PushScheduled {

    // 用来标识定时器状态 1：开启 0：关闭
    public static Integer SCHEDULED_STATUS = 0;

    // 第三方状态：0-排队中, 1-发送中，2-发送完成，3-发送失败，4-消息被撤销5-消息过期, 6-筛选结果为空，7-定时任务尚未开始处理
    @Autowired
    private IPushService iPushService;
    /**
     * 每2个小时更新一次推送状态
     */
    @Scheduled(cron = "${scheduled.push-checkStats}")
    public void checkStats(){
        LogUtil.SCHEDULED.info("定时器：推送检查");
        if (Constants.SCHEDULER_SWITCH == 0) {
            SCHEDULED_STATUS = 0;
            return;
        }
        SCHEDULED_STATUS = 1;

        List<Push> list = iPushService.list(new QueryWrapper<Push>().eq("status", 0));
        JSONObject resultjson;
        JSONObject datajson;
        String status;// 第三方的推送状态
        if (!list.isEmpty()) {
            LogUtil.SCHEDULED.info("定时器：推送检查 - list.size:{}", list.size());
            for (Push push : list) {
                if (Constants.SCHEDULER_SWITCH == 0) {
                    SCHEDULED_STATUS = 0;
                    LogUtil.SCHEDULED.info("推送定时器已关闭SCHEDULED_STATUS[{}]........", SCHEDULED_STATUS);
                    return;
                }
                if (StringUtils.isBlank(push.getTaskId())) {
                    LogUtil.SCHEDULED.info("定时器：推送检查 - push taskId is null, push id:{}", push.getId());
                    continue;
                }
                try {
                    resultjson = iPushService.taskStatus(push.getTaskId(), push.getPlatform());
                    LogUtil.SCHEDULED.info("定时器：推送检查 - push taskId:{}，taskStatus:{}", push.getTaskId(), resultjson);
                    if (resultjson != null && ConstantsEnum.PUSH_CODE_0.stringValue().equals(resultjson.getString(ConstantsEnum.PUSH_CODE.stringValue()))
                            && resultjson.getJSONObject(ConstantsEnum.PUSH_CODE_DATA.stringValue()) != null) {

                        datajson = resultjson.getJSONObject(ConstantsEnum.PUSH_CODE_DATA.stringValue());
                        // 0-排队中, 1-发送中，2-发送完成，3-发送失败，4-消息被撤销5-消息过期, 6-筛选结果为空，7-定时任务尚未开始处理
                        if (datajson != null && datajson.getJSONObject(ConstantsEnum.PUSH_ANDROID.stringValue()) != null) {// 不一定有
                            status = datajson.getJSONObject(ConstantsEnum.PUSH_ANDROID.stringValue()).getString(ConstantsEnum.PUSH_STATUS.stringValue());
                            if (ConstantsEnum.PUSH_TASK_STATUS_2.stringValue().equals(status)) {
                                push.setStatus(1);
                            }
                        }
                        if (datajson != null && datajson.getJSONObject(ConstantsEnum.PUSH_IOS.stringValue()) != null) {// 不一定有
                            status = datajson.getJSONObject(ConstantsEnum.PUSH_IOS.stringValue()).getString(ConstantsEnum.PUSH_STATUS.stringValue());
                            if (ConstantsEnum.PUSH_TASK_STATUS_2.stringValue().equals(status)) {
                                push.setStatus(1);
                            }
                        }
                        if (!push.getStatus().equals(0)) {
                            iPushService.updateById(push);
                        }
                    } else {
                        log.error("{}|定时检查推送不处理:{}", push.getId(), resultjson);
                    }
                } catch (Exception e) {
                    log.error("{}|定时检查推送结果异常:{}", push.getId(), e);
                }
            }
        }
        SCHEDULED_STATUS = 0;
    }
}
