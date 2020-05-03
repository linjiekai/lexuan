package com.zhuanbo.shop.api.scheduled;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhuanbo.core.constants.*;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserIncomeDetails;
import com.zhuanbo.core.entity.WithdrOrder;
import com.zhuanbo.core.enums.BankCode;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.LogUtil;
import com.zhuanbo.core.util.MapUtil;
import com.zhuanbo.service.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class WithdrOrderScheduled {


    // 用来标识定时器状态 1：开启 0：关闭
    public static Integer SCHEDULED_STATUS_2 = 0;
    public static Integer SCHEDULED_STATUS_3 = 0;
    private static final String STATUS_S = "S";// 充值状态：成功
    private static final String STATUS_F = "F";// 充值状态：失败
    private static final String STATUS_E = "E";// 提现状态：异常 ERROR

    @Autowired
    private ICashService iCashService;
    @Autowired
    private IUserIncomeDetailsService iUserIncomeDetailsService;
    @Autowired
    private IWithdrOrderService iWithdrOrderService;
    @Autowired
    private PlatformTransactionManager txManager;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private INotifyMsgService iNotifyMsgService;
    @Autowired
    private IPlatformIncomeDetailsService iPlatformIncomeDetailsService;

    /**
     * 提现记录状态更新
     */
    @Scheduled(cron = "${scheduled.withdraw-check}")
    public void onloadIncome2Withdraw() {
        LogUtil.SCHEDULED.info("|提现记录处理|开始");
        try {
            if (Constants.SCHEDULER_SWITCH == 0) {
                SCHEDULED_STATUS_2 = 0;
                return;
            }
            SCHEDULED_STATUS_2 = 1;

            List<WithdrOrder> withdrOrderList = iWithdrOrderService.list(
                    new QueryWrapper<WithdrOrder>().in("order_status", WithdrOrderStatusEnum.THIRD_SUCCESS.getId()));

            // 微信提现
            List<WithdrOrder> weixinWithdrOrderList = iWithdrOrderService.list(
                    new QueryWrapper<WithdrOrder>().in("order_status", WithdrOrderStatusEnum.WAIT.getId()).eq("bank_code", BankCode.WEIXIN.getId()));
            if (!weixinWithdrOrderList.isEmpty()) {
                withdrOrderList.addAll(weixinWithdrOrderList);
            }

            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = null;
            UserIncomeDetails userIncomeDetails;
            String orderNo;
            LocalDateTime now;

            for (WithdrOrder order : withdrOrderList) {

                if (Constants.SCHEDULER_SWITCH == 0) {
                    SCHEDULED_STATUS_2 = 0;
                    return;
                }
                JSONObject data = iCashService.queryWithdrOrder(order);

                //成功的处理
                if (null != data && STATUS_S.equalsIgnoreCase(data.getString(ReqResEnum.ORDER_STATUS.String()))) {
                    try {
                        status = txManager.getTransaction(def);

                        // 更新订单状态
                        order.setOrderStatus(STATUS_S);
                        order.setBankWithdrDate(DateUtil.date10());
                        order.setBankWithdrTime(DateUtil.time8());
                        order.setOutTradeNo(data.getString("outTradeNo"));
                        boolean update2 = iWithdrOrderService.update(order,
                                new UpdateWrapper<WithdrOrder>().eq("id", order.getId()).notIn("order_status", STATUS_S));
                        if (!update2) {
                            throw new RuntimeException(
                                    "定时提现状态检查update2 = false, WithdrOrder:orderNo:" + order.getOrderNo());
                        }
                        orderNo = order.getOrderNo();

                        // 记录用户收益明细
                        now = LocalDateTime.now();
                        Long userId = order.getUserId();
                        User user = iUserService.getById(userId);
                        LogUtil.SCHEDULED.info("|提现记录状态更新|记录用户收益明细|提现订单编号:{}", order.getOrderNo());
                        userIncomeDetails = new UserIncomeDetails();
                        userIncomeDetails.setUserId(userId);
                        userIncomeDetails.setOrderNo(orderNo);
                        userIncomeDetails.setChangeType(ChangeType.WITHDR.getId());
                        userIncomeDetails.setPrice(order.getPrice());
                        userIncomeDetails.setFromUserId(userId);
                        userIncomeDetails.setFromPtLevel(user.getPtLevel());
                        userIncomeDetails.setOperateIncome(order.getPrice());
                        userIncomeDetails.setUsableIncome(order.getPrice());
                        userIncomeDetails.setOperateType(UserIncomeOperateType.SUBSTRACT.getId());
                        userIncomeDetails.setIncomeType(IncomeType.WITHDR.getId());
                        userIncomeDetails.setBankCode(order.getBankCode());
                        userIncomeDetails.setBankCardNo(order.getBankCardNo());
                        userIncomeDetails.setBankCardName(order.getBankCardName());
                        userIncomeDetails.setStatus(UserIncomeStatusType.NORMAL.getId());
                        userIncomeDetails.setIncomeDate(DateUtil.toyyyy_MM_dd(now));
                        userIncomeDetails.setIncomeTime(DateUtil.toHH_mm_ss(now));
                        userIncomeDetails.setStatType(IncomeStatTypeEnum.YES.getId());
                        userIncomeDetails.setStatDate(DateUtil.toyyyy_MM_dd(now));
                        userIncomeDetails.setContent(ChangeType.WITHDR.getName());
                        userIncomeDetails.setAddTime(now);
                        userIncomeDetails.setUpdateTime(now);
                        iUserIncomeDetailsService.save(userIncomeDetails);

                        // 记录平台收益明细
                        LogUtil.SCHEDULED.info("|提现记录状态更新|记录平台收益明细|提现订单编号:{}", order.getOrderNo());
                        iPlatformIncomeDetailsService.save(userId, orderNo, PlatformIncomeOrderTypeEnum.TRADE.getId(), "",
                                order.getWithdrPrice(), PlatformIncomeOperateTypeEnum.SUBSTRACT.getId(),
                                PlatformIncomeTypeEnum.WITHDR.getId(), PlatformIncomeDetailsStatusEnum.EFFECTIVE.getId(),
                                PlatformIncomeTypeEnum.WITHDR.getName());

                        // 通知用户:"你的提现申请已审核通过，请查看到账情况"
                        iNotifyMsgService.notifyAndPush(user, ConstantsEnum.PLATFORM_ZBMALL.stringValue(),
                                1, "系统通知", "你的提现申请已通过，请查看到账情况。", MapUtil.of("type", 3, "link", ""));

                        txManager.commit(status);
                    } catch (Exception e) {
                        LogUtil.SCHEDULED.info("|提现记录状态更新|失败，orderNo:{}", order.getOrderNo());
                        log.error("|提现记录状态更新|orderNo:{}，异常:{}", order.getOrderNo(), e);
                        Optional.ofNullable(status).ifPresent(x -> txManager.rollback(x));
                    }
                }

                //失败时的处理
                if (null != data && STATUS_F.equalsIgnoreCase(data.getString(ReqResEnum.ORDER_STATUS.String()))) {
                    try {
                        status = txManager.getTransaction(def);
                        order.setOrderStatus(STATUS_F);
                        order.setOutTradeNo(data.getString("outTradeNo"));
                        boolean update2 = iWithdrOrderService.update(order,
                                new UpdateWrapper<WithdrOrder>().eq("id", order.getId()).notIn("order_status", STATUS_S));
                        if (!update2) {
                            throw new RuntimeException(
                                    "定时提现状态检查update3 = false, WithdrOrder:orderNo:" + order.getOrderNo());
                        }

                        // 通知用户:"你的提现申请未能成功，请重新申请"
                        Long userId = order.getUserId();
                        User user = iUserService.getById(userId);
                        iNotifyMsgService.notifyAndPush(user, ConstantsEnum.PLATFORM_ZBMALL.stringValue(),
                                1, "系统通知", "你的提现申请未能成功，请重新申请。", MapUtil.of("type", 3, "link", ""));

                        txManager.commit(status);
                    } catch (Exception e) {
                        LogUtil.SCHEDULED.info("|提现记录状态更新|失败，orderNo:{}", order.getOrderNo());
                        log.error("|提现记录状态更新|orderNo:{}，异常:{}", order.getOrderNo(), e);
                        Optional.ofNullable(status).ifPresent(x -> txManager.rollback(x));
                    }
                }
            }
        } finally {
            SCHEDULED_STATUS_2 = 0;
            LogUtil.SCHEDULED.info("|提现记录处理|结束");
        }

    }

    /**
     * [提现申请异常]处理
     */
    @Scheduled(cron = "${scheduled.withdr-error-check}")
    public void onloadIncome4WithdrError() {
        LogUtil.SCHEDULED.info("|提现异常记录处理|开始");
        try {
            if (Constants.SCHEDULER_SWITCH == 0) {
                SCHEDULED_STATUS_3 = 0;
                return;
            }
            SCHEDULED_STATUS_3 = 1;

            List<WithdrOrder> withdrOrderList = iWithdrOrderService
                    .list(new QueryWrapper<WithdrOrder>().in("order_status", WithdrOrderStatusEnum.ERROR.getId()));
            try {
                for (WithdrOrder order : withdrOrderList) {
                    if (Constants.SCHEDULER_SWITCH == 0) {
                        SCHEDULED_STATUS_3 = 0;
                        return;
                    }
                    JSONObject data = iCashService.withdrApplyErrorCheck(order);
                    if (null != data) {
                        // 更新状态
                        order.setOrderStatus(WithdrOrderStatusEnum.FAIL.getId());
                        boolean update = iWithdrOrderService.update(order,
                                new UpdateWrapper<WithdrOrder>().eq("id", order.getId()).eq("order_status", WithdrOrderStatusEnum.ERROR.getId()));
                        if (!update) {
                            LogUtil.SCHEDULED.info("|提现异常记录处理|fail  WithdrOrder:orderNo:{}", order.getOrderNo());
                        } else {
                            LogUtil.SCHEDULED.info("|提现异常记录处理|success WithdrOrder:orderNo:{}", order.getOrderNo());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("定时[提现申请异常]检查异常:{}", e);
            }
        } finally {
            SCHEDULED_STATUS_3 = 0;
            LogUtil.SCHEDULED.info("|提现异常记录处理|结束");
        }

    }

}
