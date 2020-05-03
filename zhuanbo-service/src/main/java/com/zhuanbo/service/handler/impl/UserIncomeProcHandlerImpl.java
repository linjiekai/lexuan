package com.zhuanbo.service.handler.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhuanbo.core.constants.Align;
import com.zhuanbo.core.constants.OrderStatus;
import com.zhuanbo.core.dto.OrderRefundDto;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.OrderRefund;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.SpringContextUtil;
import com.zhuanbo.core.vo.ResponseMsgVO;
import com.zhuanbo.service.handler.IProfitProcHandler;
import com.zhuanbo.service.handler.IUserIncomeProcHandler;
import com.zhuanbo.service.service.IOrderGoodsService;
import com.zhuanbo.service.service.IOrderRefundService;
import com.zhuanbo.service.service.IOrderService;
import com.zhuanbo.service.service.IOrderTransDetailsService;
import com.zhuanbo.service.service.IPlatformIncomeDetailsService;
import com.zhuanbo.service.service.ISeqIncrService;
import com.zhuanbo.service.service.IUserIncomeDetailsService;
import com.zhuanbo.service.service.IUserIncomeService;
import com.zhuanbo.service.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户收益利润分配处理类
 *
 * @author chenfeihang
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class UserIncomeProcHandlerImpl implements IUserIncomeProcHandler {

    @Autowired
    private IUserService userService;

    @Autowired
    private IUserIncomeService userIncomeService;

    @Autowired
    private IUserIncomeDetailsService userIncomeDetailsService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IOrderRefundService orderRefundService;

    @Autowired
    private IOrderGoodsService orderGoodsService;

    @Autowired
    private IPlatformIncomeDetailsService platformIncomeDetailsService;

    @Autowired
    private ISeqIncrService seqIncrService;
    @Autowired
    private IOrderTransDetailsService orderTransDetailsService;

    
    @Override
    public void orderRefundProc(OrderRefundDto orderRefundDto) throws Exception {
        ResponseMsgVO responseMsgVO = new ResponseMsgVO();
        Order order = orderService.getOne(new QueryWrapper<Order>().eq("order_no", orderRefundDto.getOrderNo()));

        if (null == order) {
            throw new ShopException(50001);
        }

        log.info("订单退款orderNo:{} 状态orderStatus:{} 进货类型purchType:{}", order.getOrderNo(), order.getOrderStatus(), order.getPurchType());

        if (!(order.getOrderStatus().equals(OrderStatus.WAIT_SHIP.getId())
                || order.getOrderStatus().equals(OrderStatus.WAIT_DELIVER.getId())
                || order.getOrderStatus().equals(OrderStatus.SUCCESS.getId())
                || order.getOrderStatus().equals(OrderStatus.CANCEL.getId()))) {
            throw new ShopException(50007);
        }

        //存记录，定时器扫到记录发到pay在退款
        String orderRefundNo = DateUtil.date8() + seqIncrService.nextVal("order_refund_no", 8, Align.LEFT);

        IProfitProcHandler profitProcHandler = (IProfitProcHandler) SpringContextUtil.getBean("profitProcHandler");
        //退收益
        profitProcHandler.refundProfitProc(order, responseMsgVO);
        
        //平台收益
        platformIncomeDetailsService.orderRefund(order, orderRefundNo);
        //用户交易明细表
        orderTransDetailsService.orderRefund(order, orderRefundNo);

        if (order.getPrice().doubleValue() > 0) {
            boolean flag = orderService.update(new Order(), new UpdateWrapper<Order>()
                    .setSql("order_status= '" + OrderStatus.REFUND_WAIT.getId() + "', refund_price=refund_price + " + order.getPrice().subtract(order.getReducePrice()))
                    .eq("order_no", order.getOrderNo())
                    .last(" and price >= refund_price + " + order.getPrice().subtract(order.getReducePrice()))
            );

            if (!flag) {
                throw new ShopException(50007);
            }

            OrderRefund orderRefund = new OrderRefund();
            orderRefund.setUserId(order.getUserId());
            orderRefund.setOrderNo(order.getOrderNo());
            orderRefund.setPrice(order.getPrice());
            orderRefund.setOrderRefundNo(orderRefundNo);
            orderRefund.setOrderStatus(OrderStatus.REFUND_WAIT.getId());
            orderRefund.setRefundPrice(order.getPrice().subtract(order.getReducePrice()));
            orderRefund.setRefundDate(DateUtil.date10());
            orderRefund.setRefundTime(DateUtil.time8());
            orderRefund.setAdminId(orderRefundDto.getAdminId());
            orderRefund.setOperator(orderRefundDto.getOperator());
            orderRefund.setRemark(orderRefundDto.getRemark());
            orderRefundService.save(orderRefund);
        } else if (order.getPrice().doubleValue() == 0) {
            //提货到家退款状态直接等于RS
            boolean flag = orderService.update(new Order(), new UpdateWrapper<Order>()
                    .setSql("order_status= '" + OrderStatus.REFUND_SUCCESS.getId() + "', refund_price=refund_price + " + order.getPrice().subtract(order.getReducePrice()))
                    .eq("order_no", order.getOrderNo())
                    .last(" and price >= refund_price + " + order.getPrice().subtract(order.getReducePrice()))
            );

            if (!flag) {
                throw new ShopException(50007);
            }
        } else {
        	throw new ShopException(50007);
        }

    }


}
