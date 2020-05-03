package com.zhuanbo.shop.api.scheduled;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhuanbo.client.server.client.PayClient;
import com.zhuanbo.client.server.dto.ResponseDTO;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.OrderStatus;
import com.zhuanbo.core.constants.PlatformType;
import com.zhuanbo.core.constants.ReqResEnum;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.OrderRefund;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.LogUtil;
import com.zhuanbo.core.util.Sign;
import com.zhuanbo.service.service.IOrderRefundService;
import com.zhuanbo.service.service.IOrderService;
import com.zhuanbo.service.vo.PayNotifyParamsVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 退款订单定时器
 */
@Component
@Slf4j
public class RefundOrderScheduled {

    @Autowired
    private IOrderService orderService;
    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private IOrderRefundService iOrderRefundService;
    @Autowired
    private PayClient payClient;
    @Autowired
    private RestTemplate restTemplate;

    // 用来标识定时器状态 1：开启 0：关闭
    public static Integer SCHEDULED_STATUS1 = 0;
    public static Integer SCHEDULED_STATUS2 = 0;


    //5分钟一次
    @Scheduled(cron = "${scheduled.refund-order}")
    public void refundOrder() {

        if (Constants.SCHEDULER_SWITCH == 0) {
            SCHEDULED_STATUS1 = 0;
            return;
        }
        SCHEDULED_STATUS1 = 1;

        //查询当前时间前 2分钟-前60分钟的数据
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -2);
        Date endTime = cal.getTime();
        cal.add(Calendar.MINUTE, -360);
        Date startTime = cal.getTime();
        String s = DateUtil.format(startTime, "yyyy-MM-dd HH:mm:ss");
        String s1 = DateUtil.format(endTime, "yyyy-MM-dd HH:mm:ss");
        LogUtil.SCHEDULED.info("定时器::退款订单处理:: 开始，时间段:{}", s + " - " + s1);
        try {
            List<OrderRefund> orderList = iOrderRefundService.list(
                    new QueryWrapper<OrderRefund>().eq("order_status", OrderStatus.REFUND_WAIT.getId())
                            .ge("add_time", s)
                            .le("add_time", s1)
            );

            Map<String, Object> params = null;
            String plain = null;
            String sign = null;
            for (OrderRefund order : orderList) {
                String orderNo = order.getOrderNo();
                String orderRefundNo = order.getOrderRefundNo();
                try {
                    if (Constants.SCHEDULER_SWITCH == 0) {
                        SCHEDULED_STATUS1 = 0;
                        LogUtil.SCHEDULED.info("定时器::退款订单处理::定时器已关闭SCHEDULER_SWITCH:{}", SCHEDULED_STATUS1);
                        return;
                    }
                    params = new HashMap<String, Object>();
                    params.put(ReqResEnum.METHOD_TYPE.String(), ReqResEnum.METHOD_ORDERREFUND.String());
                    params.put("requestId", System.currentTimeMillis());
                    params.put("mercId", PlatformType.ZBMALL.getId());
                    params.put("platform", PlatformType.ZBMALL.getCode());
                    params.put("orderNo", orderNo);
                    params.put("price", order.getPrice());
                    params.put("refundOrderNo", orderRefundNo);
                    params.put(ReqResEnum.X_MPMALL_SIGN_VER.String(), ReqResEnum.X_MP_SIGN_VER_V1.String());
                    plain = Sign.getPlain(params) + "&key=" + authConfig.getMercPrivateKey();
                    sign = Sign.sign(plain);

                    MDC.put("X-MPMALL-Sign-PAY", sign);
                    ResponseDTO responseDTO = payClient.unified(params);
                    //请求pay
                    Optional.ofNullable(responseDTO).orElseThrow(() -> new ShopException(10067));

                    //如果返回码不成功，跳过，处理下一条
                    if (StringUtils.isBlank(responseDTO.getCode()) || !Constants.SUCCESS_CODE.equals(responseDTO.getCode())) {
                        throw new ShopException(10067);
                    }
                    //状态改为BW
                    order.setOrderStatus(OrderStatus.REFUND_BANK_WAIT.getId());
                    iOrderRefundService.updateById(order);

                    LogUtil.SCHEDULED.info("定时器::退款订单处理:: 完成，商城订单号：{},退款订单号：{}，", orderNo, orderRefundNo);
                } catch (Exception e) {
                    LogUtil.SCHEDULED.info("定时器::退款订单处理:: 失败，商城订单号：{},退款订单号：{}，", orderNo, orderRefundNo);
                    log.error("定时器::退款订单处理:: 失败，商城订单号：{},退款订单号：{} ,error:{}，", orderNo, orderRefundNo, e);
                }
            }
        } catch (Exception e1) {
            log.error("定时器::退款订单处理:: 失败，error:{}，", e1);
        }
        SCHEDULED_STATUS1 = 0;
        LogUtil.SCHEDULED.info("定时器::退款订单处理:: 结束，开始，时间段:{}", s + "-" + s1);
    }


    //5分钟一次
    @Scheduled(cron = "${scheduled.refund-order-query}")
    public void refundOrderQuery() {

        if (Constants.SCHEDULER_SWITCH == 0) {
            SCHEDULED_STATUS2 = 0;
            return;
        }
        SCHEDULED_STATUS2 = 1;

        LogUtil.SCHEDULED.info("定时器::退款订单查询:: 开始，查询状态 ：BW");
        try {
            //查出所有BW的订单
            List<OrderRefund> orderList = iOrderRefundService.list(new QueryWrapper<OrderRefund>().eq("order_status", OrderStatus.REFUND_BANK_WAIT.getId()));

            Map<String, Object> params = null;
            Map<String, Object> resultMap = null;
            String plain = null;
            String sign = null;
            for (OrderRefund order : orderList) {
                String orderNo = order.getOrderNo();
                String orderRefundNo = order.getOrderRefundNo();
                try {
                    if (Constants.SCHEDULER_SWITCH == 0) {
                        SCHEDULED_STATUS1 = 0;
                        LogUtil.SCHEDULED.info("定时器::退款订单查询::定时器已关闭SCHEDULER_SWITCH :{}", SCHEDULED_STATUS2);
                        return;
                    }
                    params = new HashMap<String, Object>();
                    params.put(ReqResEnum.METHOD_TYPE.String(), ReqResEnum.METHOD_QUERYORDERREFUND.String());
                    params.put("requestId", System.currentTimeMillis());
                    params.put("mercId", PlatformType.ZBMALL.getId());
                    params.put("refundOrderNo", orderRefundNo);
                    params.put(ReqResEnum.X_MPMALL_SIGN_VER.String(), ReqResEnum.X_MP_SIGN_VER_V1.String());
                    plain = Sign.getPlain(params) + "&key=" + authConfig.getMercPrivateKey();
                    sign = Sign.sign(plain);

                    MDC.put("X-MPMALL-Sign-PAY", sign);
                    ResponseDTO responseDTO = payClient.unified(params);

                    if (null == responseDTO) {
                        continue;
                    }

                    //如果返回码不成功，跳过，处理下一条
                    if (StringUtils.isBlank(responseDTO.getCode()) || !Constants.SUCCESS_CODE.equals(responseDTO.getCode())) {
                        continue;
                    }
                    resultMap = (Map<String, Object>) responseDTO.getData();
                    String orderStatusPay = (String) resultMap.get("orderStatus");
                    String orderStatus = order.getOrderStatus();
                    boolean cloudRefund = false;
                    if (OrderStatus.SUCCESS.getId().equalsIgnoreCase(orderStatusPay) || "RF".equalsIgnoreCase(orderStatusPay)) {
                        orderStatus = OrderStatus.REFUND_SUCCESS.getId();
                        cloudRefund = true;
                    } else if ("F".equalsIgnoreCase(orderStatusPay)) {
                        orderStatus = OrderStatus.REFUND_FAIL.getId();
                    }
                    order.setOrderStatus(orderStatus);
                    //退款订单
                    iOrderRefundService.updateById(order);
                    //交易订单
                    orderService.update(new Order(), new UpdateWrapper<Order>().set("order_status", orderStatus).eq("order_no", orderNo));
                    if (cloudRefund) {
                        JSONObject request = JSONUtil.createObj().put("orderNo", order.getOrderNo()).put("adminUserId", order.getAdminId());
                        String map = restTemplate.postForObject(authConfig.getPhpCloudRefundUrl(), request, String.class);
                        LogUtil.SCHEDULED.info("订单orderNo：{}退款成功，通知PHP退云仓结果：{}", orderNo, map);
                    }

                    LogUtil.SCHEDULED.info("定时器::退款订单查询:: 完成，商城订单号：{},退款订单号：{}，", orderNo, orderRefundNo);
                } catch (Exception e) {
                    LogUtil.SCHEDULED.info("定时器::退款订单查询:: 失败，商城订单号：{},退款订单号：{}，", orderNo, orderRefundNo);
                    log.error("定时器::退款订单查询:: 失败，商城订单号：{},退款订单号：{} ,error:{}，", orderNo, orderRefundNo, e);
                }
            }
        } catch (Exception e1) {
            log.error("定时器::退款订单查询:: 失败，error:{}，", e1);
        }
        SCHEDULED_STATUS2 = 0;
        LogUtil.SCHEDULED.info("定时器::退款订单查询:: 结束，查询状态 ：BW");
    }

}
