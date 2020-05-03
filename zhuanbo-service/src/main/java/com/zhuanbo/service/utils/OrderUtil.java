package com.zhuanbo.service.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuanbo.core.constants.OrderStatus;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.service.service.IOrderService;

/*
 * 订单流程：下单成功－》支付订单－》发货－》收货
 * 订单状态：
 * 101 订单生成，未支付；102，下单未支付用户取消；103，下单未支付超期系统自动取消
 * 201 支付完成，商家未发货；202，订单生产，已付款未发货，用户申请退款；203，管理员执行退款操作，确认退款成功；
 * 301 商家发货，用户未确认；
 * 401 用户确认收货，订单结束； 402 用户没有确认收货，但是快递反馈已收获后，超过一定时间，系统自动确认收货，订单结束。
 *
 * 当101用户未付款时，此时用户可以进行的操作是取消或者付款
 * 当201支付完成而商家未发货时，此时用户可以退款
 * 当301商家已发货时，此时用户可以有确认收货
 * 当401用户确认收货以后，此时用户可以进行的操作是退货、删除、去评价或者再次购买
 * 当402系统自动确认收货以后，此时用户可以删除、去评价、或者再次购买
 *
 * 订单流水状态 未支付/待支付:W 已完成:S 已取消:C 待发货:WS 待收货:WD
 */
public class OrderUtil {


    public static OrderHandleOption build(Order order){
    	String status = order.getOrderStatus();
        OrderHandleOption handleOption = new OrderHandleOption();
        if (OrderStatus.WAIT_PAY.getId().equals(status)) {
            // 如果订单没有被取消，且没有支付，则可支付，可取消
            handleOption.setCancel(true);
            handleOption.setPay(true);
        }
        else if (OrderStatus.SUCCESS.getId().equals(status) || OrderStatus.CANCEL.getId().equals(status)) {
            // 如果订单已经取消或是已完成，则可删除
            handleOption.setDelete(true);
        }
        else if (OrderStatus.WAIT_SHIP.getId().equals(status)) {
            // 如果订单已付款，没有发货，则可退款
            handleOption.setRefund(true);
        }
        else if (OrderStatus.WAIT_DELIVER.getId().equals(status)) {
            // 如果订单已经发货，没有收货，则可收货操作,
            // 此时不能取消订单
            handleOption.setConfirm(true);
        } else if (OrderStatus.WAIT_AUDIT.getId().equals(status)) {
            // 待审批可以拒绝、发货

        } else if (OrderStatus.REFUND_WAIT.getId().equals(status)) { //RW

        } else if (OrderStatus.REFUND_BANK_WAIT.getId().equals(status)) {//BW

        } else if (OrderStatus.REFUND_SUCCESS.getId().equals(status)) {//S

        } else if (OrderStatus.REFUSE.getId().equals(status)) {//R

        } else if (OrderStatus.DELETE.getId().equals(status)) {//D

        } else {
            throw new IllegalStateException("status不支持");
        }

        return handleOption;
    }

    /**
     * 生成订单号：order_sn
     *
     * @param iService
     * @param userId
     * @return
     */
    public static String generateOrderNo(IOrderService iService, Integer userId) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMdd");
        String orderNo = df.format(LocalDate.now()) + getRandomNum(6);
        short retryTimes = 2;
        //自旋发现订单已经存在，这里不能无休止重试，把order_sn一样概率降到最低
        while(iService.count(new QueryWrapper<Order>().eq("user_id",userId)
                .eq("order_no",orderNo)) != 0 && retryTimes > 0){
            orderNo = getRandomNum(6);
            retryTimes--;
        }
        return orderNo;
    }
    private static String getRandomNum(Integer num) {
        String base = "0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < num; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }
}
