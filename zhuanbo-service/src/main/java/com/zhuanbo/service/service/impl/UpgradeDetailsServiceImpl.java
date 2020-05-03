package com.zhuanbo.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.config.QueueConfig;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.MapKeyEnum;
import com.zhuanbo.core.entity.DepositOrder;
import com.zhuanbo.core.entity.UpgradeDetails;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.util.LogUtil;
import com.zhuanbo.service.mapper.UpgradeDetailsMapper;
import com.zhuanbo.service.service.IDepositOrderService;
import com.zhuanbo.service.service.IMqMessageService;
import com.zhuanbo.service.service.IOrderService;
import com.zhuanbo.service.service.IOrderTransDetailsService;
import com.zhuanbo.service.service.IPlatformIncomeDetailsService;
import com.zhuanbo.service.service.IUpgradeDetailsService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.vo.PayNotifyParamsVO;
import com.zhuanbo.service.vo.UpgradeDetailsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 升级费明细表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-07-31
 */
@Service
@Slf4j
public class UpgradeDetailsServiceImpl extends ServiceImpl<UpgradeDetailsMapper, UpgradeDetails> implements IUpgradeDetailsService {

    final String S = "S";
    @Autowired
    private IDepositOrderService iDepositOrderService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private QueueConfig queueConfig;
    @Autowired
    private IOrderService iOrderService;
    @Autowired
    private IMqMessageService iMqMessageService;
    @Autowired
	private IOrderTransDetailsService iOrderTransDetailsService;
	@Autowired
	private IPlatformIncomeDetailsService iPlatformIncomeDetailsService;
	@Autowired
	private IUserService iUserService;

    @Transactional
    @Override
    public Map<String, Object> generateDetail(PayNotifyParamsVO payNotifyParamsVO) {

        LogUtil.SHARE_PROFIT.info("充值回调开始:{},", payNotifyParamsVO.getOrderNo());
        DepositOrder depositOrder = iDepositOrderService.getOne(new QueryWrapper<DepositOrder>().eq("deposit_no", payNotifyParamsVO.getOrderNo()));
        if (S.equalsIgnoreCase(depositOrder.getOrderStatus())) {
            return null;
        }
        if (!S.equalsIgnoreCase(payNotifyParamsVO.getOrderStatus())) {
            throw new RuntimeException("押金充值回调状态非S, orderNo:" + payNotifyParamsVO.getOrderNo());
        }
        
      //订单金额减去优惠金额
        BigDecimal reducePrice = new BigDecimal(0);
        if (null != payNotifyParamsVO.getReducePrice()) {
        	reducePrice = payNotifyParamsVO.getReducePrice();
        }
        if (depositOrder.getPrice().subtract(reducePrice).compareTo(payNotifyParamsVO.getPrice()) != 0) {
        	log.error("充值价格与回调价不一致，orderNo={}，depositOrderPrice={}, payOrderPrice={}", payNotifyParamsVO.getOrderNo(), depositOrder.getPrice(), payNotifyParamsVO.getPrice());
            throw new RuntimeException("充值价格与回调价不一致, orderNo:" + payNotifyParamsVO.getOrderNo());
        }

        String oldOrderStatus = depositOrder.getOrderStatus();
        /*depositOrder.setTradeType(payNotifyParamsVO.getTradeType());
        depositOrder.setPayNo(payNotifyParamsVO.getPayNo());
        depositOrder.setPayDate(payNotifyParamsVO.getPayDate());
        depositOrder.setPayTime(payNotifyParamsVO.getPayTime());
        depositOrder.setOrderStatus(S);
        depositOrder.setBankCode(payNotifyParamsVO.getBankCode());
        depositOrder.setReducePrice(reducePrice);*/

        UpdateWrapper<DepositOrder> depositOrderUpdateWrapper = new UpdateWrapper<>();
        depositOrderUpdateWrapper.set("trade_type", payNotifyParamsVO.getTradeType());
        depositOrderUpdateWrapper.set("pay_no", payNotifyParamsVO.getPayNo());
        depositOrderUpdateWrapper.set("pay_date", payNotifyParamsVO.getPayDate());
        depositOrderUpdateWrapper.set("pay_time", payNotifyParamsVO.getPayTime());
        depositOrderUpdateWrapper.set("order_status", S);
        depositOrderUpdateWrapper.set("bank_code", payNotifyParamsVO.getBankCode());
        depositOrderUpdateWrapper.set("reduce_price", reducePrice);
        depositOrderUpdateWrapper.eq("id", depositOrder.getId());
        depositOrderUpdateWrapper.eq("order_status", oldOrderStatus);

        //boolean update = iDepositOrderService.update(depositOrder, new QueryWrapper<DepositOrder>().eq("id", depositOrder.getId()).eq("order_status", oldOrderStatus));
        boolean update = iDepositOrderService.update(new DepositOrder(), depositOrderUpdateWrapper);
        if (!update) {
            throw new RuntimeException("押金充值回调更新充值记录表失败, orderNo:" + payNotifyParamsVO.getOrderNo());
        }
        UpgradeDetails upgradeDetails = new UpgradeDetails();
        upgradeDetails.setMercId(payNotifyParamsVO.getMercId());
        upgradeDetails.setPlatform(ConstantsEnum.PLATFORM_ZBMALL.stringValue());
        upgradeDetails.setOrderNo(depositOrder.getOrderNo());
        upgradeDetails.setOrderDate(payNotifyParamsVO.getOrderDate());
        upgradeDetails.setOrderTime(payNotifyParamsVO.getOrderTime());
        upgradeDetails.setUserId(depositOrder.getUserId());
        upgradeDetails.setPrice(depositOrder.getPrice());
        upgradeDetails.setPayDate(payNotifyParamsVO.getPayDate());
        upgradeDetails.setPayTime(payNotifyParamsVO.getPayTime());
        upgradeDetails.setPayType(1);
        upgradeDetails.setRefundFlag(0);
        upgradeDetails.setAdminId(0);
        upgradeDetails.setOperator("");
        this.save(upgradeDetails);
        
        User user = iUserService.getById(depositOrder.getUserId());
        
        /**
         * 由php通过MQ同步  OrderBuyNumReceiver
        //登记用户交易明细
        OrderTransDetails orderTransDetails = new OrderTransDetails();
        orderTransDetails.setOrderNo(depositOrder.getDepositNo());
        orderTransDetails.setUserId(user.getId());
        orderTransDetails.setPtLevel(user.getPtLevel());
        orderTransDetails.setTransDate(DateUtil.date10());
        orderTransDetails.setTransTime(DateUtil.date8());
        orderTransDetails.setPrice(depositOrder.getPrice());
        orderTransDetails.setBuyNum(0);
        orderTransDetails.setOperateType(1);
        orderTransDetails.setPurchType(1);
        orderTransDetails.setContent("身份升级");
        iOrderTransDetailsService.save(orderTransDetails);

        // 平台收益明细记录
        iPlatformIncomeDetailsService.save(depositOrder.getUserId(), depositOrder.getDepositNo(), PlatformIncomeOrderTypeEnum.TRADE.getId(),
                "", depositOrder.getPrice(), PlatformIncomeOperateTypeEnum.ADD.getId(),
                1, PlatformIncomeDetailsStatusEnum.EFFECTIVE.getId(), "身份升级");

         */
        
        Map<String, Object> mqMessageData = iOrderService.mqMessageData(payNotifyParamsVO.getOrderNo(), depositOrder.getOrderType(), depositOrder.getTypeSplit());
        iMqMessageService.n2Save(0, queueConfig.getExchange(), JSON.toJSONString(mqMessageData),
                queueConfig.getQueues().getOrderProfit().getRoutingKey(), mqMessageData.get("uuid").toString());

        HashMap<String, Object> backHashMap = new HashMap<>();
        backHashMap.put(MapKeyEnum.MQ_DATA.value(), mqMessageData);

        LogUtil.SHARE_PROFIT.info("充值回调结束:{},", payNotifyParamsVO.getOrderNo());
        return backHashMap;

    }


    /**
     * 管理后台获取升级费明细列表
     * @param page
     * @param limit
     * @param userId
     * @param mobile
     * @return
     */
    @Override
    public List<UpgradeDetailsVo> findUpgradeDetails(Integer page, Integer limit, Long userId, String mobile) {
        return baseMapper.findUpgradeDetails(page,limit,userId,mobile);
    }

    /**
     *  统计直属达人的数量
     * @param userId
     * @return
     */
    @Override
    public Integer countDarenNum(Long userId) {
        return baseMapper.countDarenNum(userId);
    }

    /**
     * 统计后台充值总记录数
     * @param userId
     * @param mobile
     * @return
     */
    @Override
    public Integer countUpgradeTotalRecords(Long userId, String mobile) {
        return baseMapper.countUpgradeTotalRecords(userId,mobile);
    }

    /**
     *根据手机号码获取用户信息
     * @param mobile
     * @return
     */
    @Override
    public List<UpgradeDetailsVo> findUserByMobile(String mobile) {
        return baseMapper.findUserByMobile(mobile);
    }
}
