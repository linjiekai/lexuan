package com.zhuanbo.shop.api.scheduled;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhuanbo.core.constants.*;
import com.zhuanbo.core.entity.DepositOrder;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserIncomeDetails;
import com.zhuanbo.core.entity.WithdrOrder;
import com.zhuanbo.core.enums.BankCode;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.LogUtil;
import com.zhuanbo.core.util.MapUtil;
import com.zhuanbo.service.service.*;
import com.zhuanbo.service.vo.CashResultVO;
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

/**
 * 对收益明细的钱进行后期转变
 */
@Component
@Slf4j
public class IncomeDetailsScheduled {

    // 用来标识定时器状态 1：开启 0：关闭
    public static Integer SCHEDULED_STATUS_1 = 0;
    public static Integer SCHEDULED_STATUS_2 = 0;
    public static Integer SCHEDULED_STATUS_3 = 0;
    private static final String STATUS_S = "S";// 充值状态：成功
    private static final String STATUS_F = "F";// 充值状态：失败
    private static final String STATUS_E = "E";// 提现状态：异常 ERROR

    @Autowired
    private IDepositOrderService iDepositOrderService;
    @Autowired
    private ICashService iCashService;
    @Autowired
    private PlatformTransactionManager txManager;


    /**
     * 处理待充值的数据(收益)
     */
    @Scheduled(cron = "${scheduled.deposit-check}")
    public void depositCheck() {

        if (Constants.SCHEDULER_SWITCH == 0) {
            SCHEDULED_STATUS_1 = 0;
            return;
        }
        SCHEDULED_STATUS_1 = 1;
        LogUtil.SCHEDULED.info("处理待充值的数据...");
        // 一天内的数据
        LocalDateTime time = LocalDateTime.now().plusDays(-30);
        List<DepositOrder> depositOrderList = iDepositOrderService.list(
                new QueryWrapper<DepositOrder>().in("order_status", ConstantsEnum.DEPOSIT_ORDER_STATUS_W.stringValue())
                        .in("busi_type", ConstantsEnum.DEPOSIT_BUSI_TYPE_04.stringValue(),
                                ConstantsEnum.DEPOSIT_BUSI_TYPE_07.stringValue())
                        .ge("add_time", time));

        CashResultVO vo = null;
        for (DepositOrder depositOrder : depositOrderList) {

            try {
                if (Constants.SCHEDULER_SWITCH == 0) {
                    SCHEDULED_STATUS_1 = 0;
                    return;
                }
                if (!TradeCode.ADJUSTMENT.getId().equals(depositOrder.getTradeCode())
                        && ConstantsEnum.DEPOSIT_BUSI_TYPE_05.stringValue().equals(depositOrder.getBusiType())) {
                    continue;
                }
                if (TradeCode.ADJUSTMENT.getId().equals(depositOrder.getTradeCode())
                        && ConstantsEnum.DEPOSIT_BUSI_TYPE_09.stringValue().equals(depositOrder.getBusiType())) {
                    // 扣减保证金,查询余额是否充足
                    boolean marginFlag = iDepositOrderService.checkMarginBalance(depositOrder);
                    if (!marginFlag) {
                        continue;
                    }
                }
                vo = iCashService.charge(depositOrder);
                if (null != vo && vo.getCode() != null && Constants.SUCCESS_CODE.equals(vo.getCode())) {
                    iDepositOrderService.update(new DepositOrder(),
                            new UpdateWrapper<DepositOrder>()
                                    .set("order_status", ConstantsEnum.DEPOSIT_ORDER_STATUS_BW.stringValue())
                                    .eq("deposit_no", depositOrder.getDepositNo()));
                }
            } catch (Exception e) {
                log.error("定时处理充值记录失败：{}", e);
            }
        }

        LogUtil.SCHEDULED.info("充值订单状态查询...");

        depositOrderList = iDepositOrderService.list(
                new QueryWrapper<DepositOrder>().in("order_status", ConstantsEnum.DEPOSIT_ORDER_STATUS_W.stringValue(),
                        ConstantsEnum.DEPOSIT_ORDER_STATUS_BW.stringValue()).ge("add_time", time));

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;

        CashResultVO cashResultVO2 = null;
        for (DepositOrder depositOrder : depositOrderList) {
            if (Constants.SCHEDULER_SWITCH == 0) {
                SCHEDULED_STATUS_1 = 0;
                return;
            }

            try {
                cashResultVO2 = iCashService.queryOrder(depositOrder.getDepositNo());
                LogUtil.SCHEDULED.info("处理待充值的数据...depositNo:{}..结果：{}", depositOrder.getDepositNo(), cashResultVO2);
                if (cashResultVO2 != null && STATUS_S.equalsIgnoreCase(cashResultVO2.getOrderStatus())) {
                    status = txManager.getTransaction(def);

                    iDepositOrderService.finishDeposit(depositOrder);

                    txManager.commit(status);
                }
            } catch (Exception e) {
                log.error("定时处理充值记录失败：{}", e);
                Optional.ofNullable(status).ifPresent(x -> txManager.rollback(x));
            }
        }

        SCHEDULED_STATUS_1 = 0;
    }



}
