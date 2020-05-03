package com.zhuanbo.shop.api.handler.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cn.hutool.core.collection.CollUtil;
import com.zhuanbo.core.constants.*;
import com.zhuanbo.core.dto.OrderRefundDto;
import com.zhuanbo.service.handler.IUserIncomeProcHandler;
import com.zhuanbo.service.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhuanbo.core.config.QueueConfig;
import com.zhuanbo.core.entity.Dictionary;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.OrderDescribe;
import com.zhuanbo.core.entity.OrderGoods;
import com.zhuanbo.core.entity.OrderTransDetails;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.JacksonUtil;
import com.zhuanbo.core.util.LogUtil;
import com.zhuanbo.core.util.MapUtil;
import com.zhuanbo.service.service3rd.rabbitmq.IRabbitMQSenderService;
import com.zhuanbo.service.service3rd.rabbitmq.impl.RabbitMQSenderImpl;
import com.zhuanbo.service.vo.NotifyPushMQVO;
import com.zhuanbo.service.vo.PayNotifyParamsVO;
import com.zhuanbo.shop.api.handler.IOrderProcHandler;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class OrderProcHandlerImpl implements IOrderProcHandler {

    @Autowired
    private IOrderService iOrderService;
    @Autowired
    private IOrderGoodsService iOrderGoodsService;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private QueueConfig queueConfig;
    @Autowired
    private IMqMessageService iMqMessageService;
    @Autowired
    private IRabbitMQSenderService iRabbitMQSenderService;
    @Autowired
    private IOrderTransDetailsService iOrderTransDetailsService;
    @Autowired
    private IOrderDescribeService iOrderDescribeService;
    @Autowired
    private IPlatformIncomeDetailsService iPlatformIncomeDetailsService;
    @Autowired
    private IDictionaryService iDictionaryService;
    @Autowired
    private IUserIncomeProcHandler iUserIncomeProcHandler;
    @Autowired
    private INotifyMsgService iNotifyMsgService;
    @Autowired
    private IPushService iPushService;

    @Override
    public Map<String, Object> proc(PayNotifyParamsVO vo) throws Exception {

        Order order = iOrderService.getOne(new QueryWrapper<Order>().eq("order_no", vo.getOrderNo()));

        if (null == order) {
            log.error("订单号orderNo[{}]不存在", vo.getOrderNo());
            throw new ShopException(50001);
        }

        // 记录用户下单时的等级
        User user = iUserService.getOne(new QueryWrapper<User>().eq("id", order.getUserId()));

        //支付成功，但订单已取消，若是则发起退款操作，发通知，结束
        if (OrderStatus.CANCEL.getId().equalsIgnoreCase(order.getOrderStatus())) {
            OrderRefundDto orderRefundDto = new OrderRefundDto();
            orderRefundDto.setOrderNo(order.getOrderNo());
            orderRefundDto.setRemark("支付成功但订单已取消，自动发起退款");
            orderRefundDto.setReducePrice(vo.getReducePrice());
            iUserIncomeProcHandler.orderRefundProc(orderRefundDto);
            //通知
            NotifyPushMQVO notifyPushMQVO = new NotifyPushMQVO(user.getId(), user.getNickname(), PlatformType.ZBMALL.getCode(), "系统通知", CopyWritingConstants.PAY_SUCCESS_ORDER_CANCEL, 1, MapUtil.of("type", 3, "link", ""),"");
            iPushService.push(queueConfig.getExchange(), queueConfig.getQueues().getNotifyPush().getRoutingKey(), CollUtil.newLinkedList(notifyPushMQVO));
            return null;
        }

        if (!OrderStatus.WAIT_PAY.getId().equals(order.getOrderStatus())) {
            log.error("订单号orderNo[{}]状态[{}]不正确", order.getOrderNo(), order.getOrderStatus());
            throw new ShopException(50007);
        }

        //订单金额减去优惠金额
        BigDecimal reducePrice = new BigDecimal(0);
        if (null != vo.getReducePrice()) {
        	reducePrice = vo.getReducePrice();
        }
        
        if (order.getPrice().subtract(reducePrice).compareTo(vo.getPrice()) != 0) {
            log.error("订单号orderNo[{}], 支付金额[{}] != 订单金额[{}]", vo.getOrderNo(), vo.getPrice(), order.getPrice());
            throw new ShopException("50008");
        }

        vo.setPrice(order.getPrice());
        BeanUtils.copyProperties(vo, order);
        log.info("支付转化：{}", JacksonUtil.objTojson(order));

        order.setOrderStatus(OrderStatus.WAIT_SHIP.getId());
        Dictionary dictionary = iDictionaryService.getOne(new QueryWrapper<Dictionary>().eq("category", "bankcode").eq("str_val", order.getBankCode()).last("limit 1"));
        Optional.ofNullable(dictionary).ifPresent(s -> {
            order.setBankName(dictionary.getName());
        });

        // 赠品 - 订单购买人更换
        if (BuyTypeEnum.BUY_TYPE_2.value().equals(order.getBuyType())) {
            OrderDescribe orderDescribe = iOrderDescribeService.getOne(new QueryWrapper<OrderDescribe>()
                    .eq("order_no", order.getOrderNo()));
            User receive = iUserService.getOne(new QueryWrapper<User>()
                    .eq("mobile", orderDescribe.getReceiveGiftMobile())
                    .eq("deleted", 0));
            order.setNickname(receive.getNickname());
            order.setUserName(receive.getUserName());
            order.setUserId(receive.getId());
            user = receive;
        }
        // 记录用户下单时的等级
        order.setPtLevel(user.getPtLevel());

        boolean updateFlag = iOrderService.update(order, new UpdateWrapper<Order>().eq("order_no", order.getOrderNo())
                .eq("order_status", OrderStatus.WAIT_PAY.getId()));

        //订单状态已被其它并发请求修改，异常返回
        if (!updateFlag) {
            log.error("订单号orderNo[{}]状态不正确", order.getOrderNo(), order.getOrderStatus());
            throw new ShopException(50007);
        }
        // 商品购买者数量添加
        List<OrderGoods> orderGoodsList = iOrderGoodsService.list(new QueryWrapper<OrderGoods>().eq("order_no", order.getOrderNo()).orderByAsc("goods_id"));
        Integer num = 0;
        for (OrderGoods orderGoods : orderGoodsList) {
            //iGoodsService.updateBuyerNumber(orderGoods.getGoodsId(), 1);
            num += orderGoods.getNumber();
        }

        //登记用户交易明细
        OrderTransDetails orderTransDetails = new OrderTransDetails();
        orderTransDetails.setOrderNo(order.getOrderNo());
        orderTransDetails.setUserId(user.getId());
        orderTransDetails.setPtLevel(user.getPtLevel());
        orderTransDetails.setTransDate(DateUtil.date10());
        orderTransDetails.setTransTime(DateUtil.date8());
        orderTransDetails.setPrice(order.getPrice());
        orderTransDetails.setBuyNum(num);
        orderTransDetails.setOperateType(1);
        orderTransDetails.setPurchType(order.getPurchType());
        orderTransDetails.setContent("购买商品");
        iOrderTransDetailsService.save(orderTransDetails);

        // 平台收益明细记录
        Integer purchType = order.getPurchType();
        boolean platformIncomeFlag = true;
        PlatformIncomeTypeEnum platformIncomeTypeEnum = null;
        if (purchType.intValue() == PurchType.BUY.getId()) {
            platformIncomeTypeEnum = PlatformIncomeTypeEnum.BUY;
        } else {
            platformIncomeFlag = false;
        }
        if (platformIncomeFlag) {
            iPlatformIncomeDetailsService.save(order.getUserId(), order.getOrderNo(), PlatformIncomeOrderTypeEnum.TRADE.getId(),
                    "", order.getPrice(), PlatformIncomeOperateTypeEnum.ADD.getId(),
                    platformIncomeTypeEnum.getId(), PlatformIncomeDetailsStatusEnum.EFFECTIVE.getId(), platformIncomeTypeEnum.getName());
        }

        HashMap<String, Object> backHashMap = new HashMap<>();

        // 手机通知
        NotifyPushMQVO notifyPushMQVO = new NotifyPushMQVO();
        notifyPushMQVO.setTitle(CopyWritingConstants.PUSH_TITLE);
        notifyPushMQVO.setContent(CopyWritingConstants.ORDER_GENERATE);
        notifyPushMQVO.setExtra(MapUtil.of("type", 3, "link", ""));
        notifyPushMQVO.setMsgFlag(1);
        notifyPushMQVO.setUserId(user.getId());
        notifyPushMQVO.setPlatform(ConstantsEnum.PLATFORM_ZBMALL.stringValue());
        notifyPushMQVO.setNickname(user.getNickname());
        notifyPushMQVO.setAction(PushActionEnum.ORDER_PAY.value());
        backHashMap.put(MapKeyEnum.PUSH_NOTIFY_ORDER.value(), notifyPushMQVO);
        
        List<Integer> typeSplit = new ArrayList<Integer>();
        typeSplit.add(OrderMQTypeEnum.ORDER_TYPE_F_1.value());
        // 分润消息通知
        Map<String, Object> mqMessageData = iOrderService.mqMessageData(order.getOrderNo(), OrderMQTypeEnum.ORDER_TYPE_F_1.value(), typeSplit);
        iMqMessageService.n2Save(0, queueConfig.getExchange(), JSON.toJSONString(mqMessageData),
                queueConfig.getQueues().getOrderProfit().getRoutingKey(), mqMessageData.get("uuid").toString());


        backHashMap.put(MapKeyEnum.MQ_DATA.value(), mqMessageData);
        return backHashMap;
    }

    @Override
    public void afterProc(Map<String, Object> data) {
        if (data != null) {
            if (data.containsKey(MapKeyEnum.MQ_DATA.value())) {
                Map<String, Object> mqData = (Map<String, Object>) data.get(MapKeyEnum.MQ_DATA.value());
                LogUtil.SHARE_PROFIT.info("订单分润通知:" + JSON.toJSONString(mqData));
                iRabbitMQSenderService.send(RabbitMQSenderImpl.SHOP_PROFIT_ORDER, mqData);
            }
            if (data.containsKey(MapKeyEnum.PUSH_NOTIFY_ORDER.value())) {
                NotifyPushMQVO notifyPushMQVO = (NotifyPushMQVO) data.get(MapKeyEnum.PUSH_NOTIFY_ORDER.value());
                iRabbitMQSenderService.send(RabbitMQSenderImpl.PUSH_NOTIFY, notifyPushMQVO);
            }
        }
    }
}
