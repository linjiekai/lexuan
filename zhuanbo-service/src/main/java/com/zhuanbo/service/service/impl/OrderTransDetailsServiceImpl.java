package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zhuanbo.core.constants.GoodsTypeEnum;
import com.zhuanbo.core.constants.OrderStatus;
import com.zhuanbo.core.constants.PlatformIncomeDetailsStatusEnum;
import com.zhuanbo.core.constants.PurchType;
import com.zhuanbo.core.constants.TransDetailsOrderTypeEnum;
import com.zhuanbo.core.constants.TransDetailsStatusEnum;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.OrderTransDetails;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.vo.TransDetailListVO;
import com.zhuanbo.service.mapper.OrderTransDetailsMapper;
import com.zhuanbo.service.service.IOrderGoodsService;
import com.zhuanbo.service.service.IOrderTransDetailsService;
import com.zhuanbo.service.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户交易明细表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-11-05
 */
@Slf4j
@Service
public class OrderTransDetailsServiceImpl extends ServiceImpl<OrderTransDetailsMapper, OrderTransDetails> implements IOrderTransDetailsService {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IOrderGoodsService iOrderGoodsService;

    @Override
    public Map<String, Object> toMap(Long uid, List<OrderTransDetails> orderTransDetailsList) {

        Map<String, Object> backMap = Maps.newHashMap();

        if (CollectionUtils.isEmpty(orderTransDetailsList)) {
            backMap.put("items", Lists.newArrayList());
            backMap.put("totalCount", 0);
            return backMap;
        }

        User user = iUserService.getById(uid);
        Integer totalCount = 0;
        List<TransDetailListVO> transDetailListVOList = new ArrayList<>();
        TransDetailListVO transDetailListVO;

        for (OrderTransDetails orderTransDetails : orderTransDetailsList) {

            transDetailListVO = new TransDetailListVO();
            BeanUtils.copyProperties(orderTransDetails, transDetailListVO);
            transDetailListVO.setHeadImgUrl(user.getHeadImgUrl());
            transDetailListVO.setNickname(user.getNickname());
            transDetailListVOList.add(transDetailListVO);
            totalCount += orderTransDetails.getBuyNum();
        }

        backMap.put("items", transDetailListVOList);
        backMap.put("totalCount", totalCount);
        return backMap;
    }


    @Override
    public Map<String, Object> getCurrnetPeriodMsg(Long userId) throws ParseException {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if(null == userId){
            return resultMap;
        }

        Date date = new Date();
        int day = DateUtil.getDate(date);
        String startDate ;
        String endDate ;
        Calendar calendar = Calendar.getInstance();
        if (day < 20) {
            //上个月20号-这个月19号
            calendar.setTime(date); // 设置为当前时间
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1); // 设置为上一个月
            calendar.set(Calendar.DAY_OF_MONTH, 20);
            Date last = calendar.getTime();
            startDate = DateUtil.dateFormat(last, DateUtil.DATE_PATTERN); //上个月20号
            calendar.setTime(date); // 设置为当前时间
            calendar.set(Calendar.DAY_OF_MONTH, 19);
            Date now = calendar.getTime();
            endDate = DateUtil.dateFormat(now, DateUtil.DATE_PATTERN);//这个月19号
        } else {
            //这个月20号-下个月19
            calendar.setTime(date); // 设置为当前时间
            calendar.set(Calendar.DAY_OF_MONTH, 20);
            Date last = calendar.getTime();
            startDate = DateUtil.dateFormat(last, DateUtil.DATE_PATTERN); //上个月20号
            calendar.setTime(date); // 设置为当前时间
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) +1); // 设置为上一个月
            calendar.set(Calendar.DAY_OF_MONTH, 19);
            Date now = calendar.getTime();
            endDate = DateUtil.dateFormat(now, DateUtil.DATE_PATTERN);//这个月19号
        }
        // 本账期
        List<OrderTransDetails> list = list(new QueryWrapper<OrderTransDetails>()
                .eq("user_id", userId)
                .ge("trans_date", startDate)
                .le("trans_date", endDate)
                .in("purch_type", Lists.newArrayList(PurchType.BUY.getId(), PurchType.ONLINE.getId(), PurchType.CLOUD.getId())));
        Integer currentPeriodCount = 0;//本账期订购
        for(OrderTransDetails item : list){
            if (TransDetailsStatusEnum.EFFECTIVE.getId() == item.getStatus()) {
                if (TransDetailsOrderTypeEnum.TRADE.getId() == item.getOrderType()) {
                    currentPeriodCount += item.getBuyNum();
                } else if (TransDetailsOrderTypeEnum.REFUND.getId() == item.getOrderType()){
                    currentPeriodCount -= item.getBuyNum();
                }
            }
        }
        resultMap.put("currentPeriodCount",currentPeriodCount);
        String currentPeriod = startDate.substring(5).replaceAll("-","/")+"-"+endDate.substring(5).replaceAll("-","/");
        resultMap.put("currentPeriod",currentPeriod);

        return resultMap;

    }

    /**
     * 退款
     *
     * @param order
     */
    @Override
    public void orderRefund(Order order, String orderRefundNo) {
        String orderNo = order.getOrderNo();
        String orderStatus = order.getOrderStatus();
        OrderTransDetails transDetails = getOne(new QueryWrapper<OrderTransDetails>().eq("order_no", orderNo).eq("order_type", TransDetailsOrderTypeEnum.TRADE.getId()));
        if (transDetails == null) {
            log.error("|用户交易明细表|退款|无用户交易明细,订单号:{}", orderNo);
            return;
        }
        Integer status = transDetails.getStatus();
        if (!OrderStatus.SUCCESS.getId().equals(orderStatus)) {
            if (status != TransDetailsStatusEnum.EXPIRED.getId()) {
                transDetails.setStatus(PlatformIncomeDetailsStatusEnum.EXPIRED.getId());
                updateById(transDetails);
            }
            return;
        }
        Integer buyNum = iOrderGoodsService.buyNumCount(order.getOrderNo(), GoodsTypeEnum.GOODS.getId());
        Long userId = order.getUserId();
        User user = iUserService.getById(userId);
        OrderTransDetails orderTransDetails = new OrderTransDetails();
        orderTransDetails.setUserId(order.getUserId());
        orderTransDetails.setPtLevel(user.getPtLevel());
        orderTransDetails.setOrderNo(orderNo);
        orderTransDetails.setOrderType(TransDetailsOrderTypeEnum.REFUND.getId());
        orderTransDetails.setSourceOrderNo(orderRefundNo);
        orderTransDetails.setPrice(order.getPrice());
        orderTransDetails.setBuyNum(buyNum);
        orderTransDetails.setOperateType(2);
        orderTransDetails.setPurchType(order.getPurchType());
        orderTransDetails.setTransDate(DateUtil.date10());
        orderTransDetails.setTransTime(DateUtil.time8());
        orderTransDetails.setContent("退款");
        save(orderTransDetails);
    }
}
