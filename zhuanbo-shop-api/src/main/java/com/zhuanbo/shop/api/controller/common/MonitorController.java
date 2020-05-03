package com.zhuanbo.shop.api.controller.common;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhuanbo.core.annotation.UnAuthAnnotation;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.shop.api.mq.LiveUserReceiver;
import com.zhuanbo.shop.api.mq.OrderFinishReceiver;
import com.zhuanbo.shop.api.mq.OrderProfitReceiver;
import com.zhuanbo.shop.api.mq.UserIncomeReceiver;
import com.zhuanbo.shop.api.scheduled.BatchDayScheduled;
import com.zhuanbo.shop.api.scheduled.IncomeDetailsScheduled;
import com.zhuanbo.shop.api.scheduled.MQMessageScheduled;
import com.zhuanbo.shop.api.scheduled.NotifyMsgPoolScheduled;
import com.zhuanbo.shop.api.scheduled.OrderScheduled;
import com.zhuanbo.shop.api.scheduled.PushScheduled;
import com.zhuanbo.shop.api.scheduled.RefundOrderScheduled;
import com.zhuanbo.shop.api.scheduled.ShipScheduled;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/monitor")
@Slf4j
public class MonitorController {


    @GetMapping("/scheduled")
    public void scheduled(HttpServletRequest request, HttpServletResponse response, String info) {
        Integer allStatus = 0;
        try {
            log.info("定时器监控请求info[{}]", info);
            switch (info) {
                case "status":
                    allStatus = ShipScheduled.SCHEDULED_STATUS
                            + OrderScheduled.SCHEDULED_STATUS
                            + OrderScheduled.SCHEDULED_STATUS2
                            + OrderScheduled.SCHEDULED_STATUS3
                            + ShipScheduled.SCHEDULED_STATUS
                            + PushScheduled.SCHEDULED_STATUS
                            + IncomeDetailsScheduled.SCHEDULED_STATUS_1
                            + IncomeDetailsScheduled.SCHEDULED_STATUS_2
                            + IncomeDetailsScheduled.SCHEDULED_STATUS_3
                            + NotifyMsgPoolScheduled.SCHEDULED_STATUS
                            + MQMessageScheduled.SCHEDULED_STATUS
                            + BatchDayScheduled.SCHEDULED_STATUS
                            + BatchDayScheduled.SCHEDULED_STATUS2
                            + BatchDayScheduled.SCHEDULED_STATUS3
                    ;
                    break;
                case "start":
                    Constants.SCHEDULER_SWITCH = 1;
                    allStatus = 1;
                    break;
                case "stop":
                    Constants.SCHEDULER_SWITCH = 0;
                    allStatus = 0;
                    break;
                default:
                    Constants.SCHEDULER_SWITCH = 0;
                    allStatus = 0;
            }
            log.info("定时器监控结果allStatus[{}]", allStatus);
            response.getOutputStream().write(("" + allStatus).getBytes());
        } catch (Exception e) {
            log.error("定时器监控", e);
            try {
                response.getOutputStream().write(("" + allStatus).getBytes());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @GetMapping("/mq")
    public void mq(HttpServletRequest request, HttpServletResponse response, String info) {
        Integer allStatus = 0;
        try {
            log.info("定时器监控请求info[{}]", info);
            switch (info) {
                case "status":
                    allStatus = OrderFinishReceiver.MQ_STATUS 
                    + OrderFinishReceiver.MQ_STATUS_2 
                    + OrderProfitReceiver.MQ_STATUS 
                    + LiveUserReceiver.MQ_STATUS
                    + UserIncomeReceiver.MQ_STATUS
                    ;
                    break;
                case "start":
                    Constants.MQ_SWITCH = 1;
                    allStatus = 1;
                    break;
                case "stop":
                    Constants.MQ_SWITCH = 0;
                    allStatus = 1;
                    break;
                default:
                    allStatus = 0;
            }
            log.info("定时器监控结果allStatus[{}]", allStatus);
            response.getOutputStream().write(("" + allStatus).getBytes());
        } catch (Exception e) {
            log.error("定时器监控", e);
            try {
                response.getOutputStream().write(("" + allStatus).getBytes());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Value("${scheduled.switch}")
    public void setSwitch(Integer scheduledSwitch) {
        Constants.SCHEDULER_SWITCH = scheduledSwitch;
    }


    @Autowired
    RefundOrderScheduled refundOrderScheduled;

    @GetMapping("/scheduledExecute")
    @UnAuthAnnotation
    public Object scheduledExecute() {
        refundOrderScheduled.refundOrder();
        return ResponseUtil.ok();
    }


}
