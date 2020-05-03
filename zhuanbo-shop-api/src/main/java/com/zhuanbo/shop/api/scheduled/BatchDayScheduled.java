package com.zhuanbo.shop.api.scheduled;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.entity.BatchDay;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.LogUtil;
import com.zhuanbo.service.service.IAppVersionService;
import com.zhuanbo.service.service.IBatchDayService;
import com.zhuanbo.service.service.IStatIncomeDayService;
import com.zhuanbo.service.service.IStatUserSaleDayService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BatchDayScheduled {

    @Autowired
    private IBatchDayService iBatchDayService;
    @Autowired
    private IStatUserSaleDayService iStatUserSaleDayService;
    @Autowired
    private IStatIncomeDayService iStatIncomeDayService;
    @Autowired
    private IAppVersionService iAppVersionService;

    // 用来标识定时器状态 1：开启 0：关闭
    public static Integer SCHEDULED_STATUS = 0;
    // 用来标识定时器状态 1：开启 0：关闭
    public static Integer SCHEDULED_STATUS2 = 0;
    // 用来标识定时器状态 1：开启 0：关闭
    public static Integer SCHEDULED_STATUS3 = 0;
    public static Integer SCHEDULED_STATUS4= 0;

    @Scheduled(cron = "${scheduled.batch-day}")
    public void execute() {

        if (Constants.SCHEDULER_SWITCH == 0) {
            SCHEDULED_STATUS = 0;
            LogUtil.SCHEDULED.info("定时器::日切::开关关闭");
            return;
        }

        SCHEDULED_STATUS = 1;

        String yesterday = DateUtil.date10(DateUtil.beforeDay(1));
        LogUtil.SCHEDULED.info("定时器::日切::开始，date:{}", yesterday);
        BatchDay batchDay = null;
        String batchDate = null;
        try {

            boolean flag = true;

            while (flag) {

                batchDay = iBatchDayService.getOne(new QueryWrapper<BatchDay>().orderByDesc("batch_date").last(" limit 1"));

                LogUtil.SCHEDULED.info("定时器::日切::上一次日切, date[{}], status[{}]", batchDay.getBatchDate(), batchDay.getStatus());

                if (yesterday.compareTo(batchDay.getBatchDate()) <= 0) {
                    flag = false;
                    break;
                }
                if (yesterday.compareTo(batchDay.getBatchDate()) > 0) {
                    batchDate = DateUtil.date10(DateUtil.afterDay(DateUtil.formatString2Timestamp(batchDay.getBatchDate(), "yyyy-MM-dd"), 1));
                    batchDay = new BatchDay();
                    batchDay.setBatchDate(batchDate);
                    batchDay.setStatus(2);
                    iBatchDayService.save(batchDay);
                }
            }
            List<BatchDay> list = iBatchDayService.list(new QueryWrapper<BatchDay>().gt("status", 1));
            for (BatchDay obj : list) {
                switch (obj.getStatus()) {
                    case 2:
                        LogUtil.SCHEDULED.info("定时器::日切::保存用户销量, date[{}], status[{}]", obj.getBatchDate(), obj.getStatus());
                        iStatUserSaleDayService.saveUserSale(obj.getBatchDate());
                        obj.setStatus(3);
                        iBatchDayService.updateById(obj);
                    case 3:
                        LogUtil.SCHEDULED.info("定时器::日切::收益日统计报表, date[{}], status[{}]", obj.getBatchDate(), obj.getStatus());
                        iStatIncomeDayService.statIncomeDay(obj.getBatchDate());
                        obj.setStatus(6);
                        iBatchDayService.updateById(obj);
                    default:
                        break;
                }
                obj.setStatus(1);
                iBatchDayService.updateById(obj);
            }
        } catch (Exception e) {
            log.error("定时器::日切::date：{}，异常：{}",batchDay.getBatchDate(), e);
        }
        SCHEDULED_STATUS = 0;
        LogUtil.SCHEDULED.info("定时器::日切::完成，date:{}", yesterday);

    }

    @Scheduled(cron = "${scheduled.batch-day-hour}")
    public void hour() {
        if (Constants.SCHEDULER_SWITCH == 0) {
            SCHEDULED_STATUS2 = 0;
            LogUtil.SCHEDULED.info("定时器::更新课时费::开关关闭");
            return;
        }

        LogUtil.SCHEDULED.info("定时器::更新课时费::开始");
        SCHEDULED_STATUS2 = 1;
        try {
//            iUserIncomeService.train2Withdraw();
        } catch (Exception e) {
            log.error("定时器::更新课时费::异常：{}", e);
        }
        SCHEDULED_STATUS2 = 0;
        LogUtil.SCHEDULED.info("定时器::更新课时费::完成");
    }


}
