package com.zhuanbo.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.config.QueueConfig;
import com.zhuanbo.core.constants.Align;
import com.zhuanbo.core.constants.ChangeType;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.IncomeAdjustStatusEnum;
import com.zhuanbo.core.constants.IncomeDetailEnum;
import com.zhuanbo.core.constants.MapKeyEnum;
import com.zhuanbo.core.constants.OrderStatus;
import com.zhuanbo.core.constants.UserIncomeOperateType;
import com.zhuanbo.core.constants.UserIncomeStatusType;
import com.zhuanbo.core.dto.AdminDepositOrderDTO;
import com.zhuanbo.core.dto.MobileDepositOrderDTO;
import com.zhuanbo.core.entity.AdjustAccount;
import com.zhuanbo.core.entity.DepositOrder;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserIncomeDetails;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.LogUtil;
import com.zhuanbo.core.util.ReportExcel;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.mapper.DepositOrderMapper;
import com.zhuanbo.service.service.IAdjustAccountService;
import com.zhuanbo.service.service.ICashService;
import com.zhuanbo.service.service.IDepositOrderService;
import com.zhuanbo.service.service.IMqMessageService;
import com.zhuanbo.service.service.ISeqIncrService;
import com.zhuanbo.service.service.IUpgradeDetailsService;
import com.zhuanbo.service.service.IUserIncomeDetailsService;
import com.zhuanbo.service.service.IUserIncomeService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.service3rd.rabbitmq.IRabbitMQSenderService;
import com.zhuanbo.service.service3rd.rabbitmq.impl.RabbitMQSenderImpl;
import com.zhuanbo.service.vo.CashResultVO;
import com.zhuanbo.service.vo.DepositOrderVO;
import com.zhuanbo.service.vo.PayNotifyParamsVO;
import com.zhuanbo.service.vo.StatDepositOrderGroupByVo;
import com.zhuanbo.service.vo.StatDepositOrderVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * <p>
 * 充值订单表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
@Service
@Slf4j
public class DepositOrderServiceImpl extends ServiceImpl<DepositOrderMapper, DepositOrder> implements IDepositOrderService {

	@Autowired
    private AuthConfig authConfig;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private ISeqIncrService iSeqIncrService;
    @Autowired
    private IUserIncomeDetailsService iUserIncomeDetailsService;
    @Autowired
    private IUserIncomeService iUserIncomeService;
    @Autowired
    private IAdjustAccountService iAdjustAccountService;
    @Autowired
    private ICashService iCashService;
    @Autowired
    private IUpgradeDetailsService iUpgradeDetailsService;
    @Autowired
    private IRabbitMQSenderService iRabbitMQSenderService;
    @Autowired
    private IMqMessageService iMqMessageService;
    @Autowired
    private QueueConfig queueConfig;
    
    
    @Override
    public DepositOrder saveFromIncomeDetails(UserIncomeDetails userIncomeDetails, String tradeCode, String busiType, String bankCode) throws Exception{

        User user = iUserService.getById(userIncomeDetails.getUserId());

        LocalDateTime now = LocalDateTime.now();
        DepositOrder depositOrder = new DepositOrder();
        depositOrder.setMercId(authConfig.getMercId());
        depositOrder.setPlatform(ConstantsEnum.PLATFORM_ZBMALL.stringValue());
        depositOrder.setDepositNo(DateUtil.date8() + iSeqIncrService.nextVal("order_no", 8, Align.LEFT));
        depositOrder.setOrderNo(userIncomeDetails.getOrderNo());
        depositOrder.setOrderDate(DateUtil.toyyyy_MM_dd(now));
        depositOrder.setOrderTime(DateUtil.toHH_mm_ss(now));
        depositOrder.setTradeCode(tradeCode);
        depositOrder.setUserId(userIncomeDetails.getUserId());
        depositOrder.setPrice(userIncomeDetails.getOperateIncome());
        depositOrder.setBankCode(bankCode);
        depositOrder.setBusiType(busiType);
        depositOrder.setOrderStatus(ConstantsEnum.DEPOSIT_ORDER_STATUS_W.stringValue());
        depositOrder.setClientIp("127.0.0.1");
        depositOrder.setAddTime(now);
        depositOrder.setUpdateTime(now);
        depositOrder.setMobile(user == null ? null : user.getMobile());
        this.save(depositOrder);
        return depositOrder;
    }

    @Override
    public void finishDeposit(DepositOrder depositOrder) throws Exception {

        UserIncomeDetails userIncomeDetails = null;
        // 修改收益明细状态
        userIncomeDetails = iUserIncomeDetailsService.getOne(new QueryWrapper<UserIncomeDetails>().eq("source_order_no", depositOrder.getDepositNo()));
        
        Map<String, Object> backMap = null;
        if (null != userIncomeDetails) {
        	boolean update = update(new DepositOrder(), new UpdateWrapper<DepositOrder>()
    				.set("order_status", ConstantsEnum.DEPOSIT_ORDER_STATUS_S.stringValue())
    				.eq("deposit_no", depositOrder.getDepositNo())
    				);
        	
            if (!update) {
                throw new RuntimeException("充值完成后处理失败，deposit_no:" + depositOrder.getDepositNo());
            }
        	if (userIncomeDetails.getStatType() == IncomeDetailEnum.STAT_TYPE_0.Integer().intValue()) {
        		userIncomeDetails.setStatType(IncomeDetailEnum.STAT_TYPE_1.Integer());
                userIncomeDetails.setStatDate(DateUtil.toyyyy_MM_dd(LocalDateTime.now()));
                update = iUserIncomeDetailsService.update(userIncomeDetails, new UpdateWrapper<UserIncomeDetails>().eq("id", userIncomeDetails.getId()).eq("status", 1).eq("stat_type", 0));
                if (!update) {
                    throw new RuntimeException("收益明细状态修改失败，userIncomeDetailsId:" + userIncomeDetails.getId());
                }
                if (userIncomeDetails.getOperateType() == UserIncomeOperateType.ADD.getId()) {
                	iUserIncomeService.subtractUavaIncome(userIncomeDetails.getUserId(), depositOrder.getPrice());
                } else {
                	iUserIncomeService.addUavaIncome(userIncomeDetails.getUserId(), depositOrder.getPrice());
                }
        	}

            // 调账:修改[调怅记录表]状态为处理成功
            if (ChangeType.ADJUST.getId()  == userIncomeDetails.getChangeType()) {
                iAdjustAccountService.update(new AdjustAccount(), new UpdateWrapper<AdjustAccount>().set("status", IncomeAdjustStatusEnum.SUCCESS.getId()).eq("adjust_no", userIncomeDetails.getAdjustNo()));
            }
        } else if (ConstantsEnum.DEPOSIT_BUSI_TYPE_06.stringValue().equalsIgnoreCase(depositOrder.getBusiType())) {
        	if (ConstantsEnum.DEPOSIT_BUSI_TYPE_06.stringValue().equalsIgnoreCase(depositOrder.getBusiType())) {
				CashResultVO cashResultVO2 = iCashService.queryOrder(depositOrder.getDepositNo());
				log.info("处理待充值的数据...depositNo:{}..结果：{}", depositOrder.getDepositNo(), cashResultVO2);
				if (cashResultVO2 != null && ConstantsEnum.DEPOSIT_ORDER_STATUS_S.stringValue().equalsIgnoreCase(cashResultVO2.getOrderStatus())) {

					PayNotifyParamsVO payNotifyParamsVO = new PayNotifyParamsVO();
					org.springframework.beans.BeanUtils.copyProperties(cashResultVO2, payNotifyParamsVO);
					payNotifyParamsVO.setOrderNo(depositOrder.getDepositNo());
					payNotifyParamsVO.setOrderStatus(ConstantsEnum.DEPOSIT_ORDER_STATUS_S.stringValue());
					backMap = iUpgradeDetailsService.generateDetail(payNotifyParamsVO);
					
					if (backMap != null) {
						if (backMap.containsKey(MapKeyEnum.MQ_DATA.value())) {
							Map<String, Object> mqData = (Map<String, Object>) backMap.get(MapKeyEnum.MQ_DATA.value());
							LogUtil.SHARE_PROFIT.info("充值订单分润通知:" + JSON.toJSONString(mqData));
							iRabbitMQSenderService.send(RabbitMQSenderImpl.SHOP_PROFIT_ORDER, mqData);
						}
					}
				}
			}
        }
        
    }

    @Override
    public Object list(AdminDepositOrderDTO dto) {
        Map<String, Object> map = buildParams(dto);
        //总记录数
        QueryWrapper<DepositOrder> ge = new QueryWrapper<DepositOrder>().le("order_date", map.get("endPayDate")).ge("order_date", map.get("startPayDate"));
        ge.eq("order_status", map.get("order_status"));
        ge.eq("busi_type", map.get("busi_type"));
        if (null!=map.get("user_id")) {
            ge.eq("user_id", map.get("user_id"));
        }
        if (null!=map.get("pay_no")){
            ge.eq("pay_no", map.get("pay_no"));
        }
        int count = count(ge);
        List<DepositOrderVO> depositOrderVOS = baseMapper.selectDepositOrderList(map);
        depositOrderVOS.forEach( depositOrderVO -> {
            String payDate = depositOrderVO.getPayDate();
            String payTime = depositOrderVO.getPayTime();
            depositOrderVO.setPayDateTime(payDate + " " + payTime);
        });
        Map<String, Object> data = new HashMap<>();
        data.put("total", count);
        data.put("items", depositOrderVOS);
        return ResponseUtil.ok(data);
    }

    @Override
    public Object exList(AdminDepositOrderDTO dto) {
        Map<String, Object> map = new HashMap<>();
        Optional.ofNullable(StringUtils.stripToNull(dto.getMobile())).ifPresent(s -> map.put("mobile",s));
        Optional.ofNullable(StringUtils.stripToNull(dto.getName())).ifPresent(s -> map.put("name",s));
        Optional.ofNullable(StringUtils.stripToNull(dto.getPayNo())).ifPresent(s -> map.put("payNo",s));
        Optional.ofNullable(StringUtils.stripToNull(dto.getUserId())).ifPresent(s -> map.put("userId",s));
        Optional.ofNullable(StringUtils.stripToNull(dto.getStartPayDate())).ifPresent(s -> map.put("startPayDate",s));
        Optional.ofNullable(StringUtils.stripToNull(dto.getEndPayDate())).ifPresent(s -> map.put("endPayDate",s));
        String endPayDate = dto.getEndPayDate();
        String startPayDate = dto.getStartPayDate();
        if (StringUtils.isEmpty(endPayDate)) {
            endPayDate = DateUtil.formatTimestamp2String(new Date(), DateUtil.DATE_PATTERN);
        }
        //三个月前
        if (StringUtils.isEmpty(startPayDate)) {
            startPayDate = DateUtil.formatTimestamp2String(DateUtil.dateAddMonths(new Date(), -3), DateUtil.DATE_PATTERN);
        }
        map.put("endPayDate", endPayDate);
        map.put("startPayDate", startPayDate);
        map.put("busiType","02"); //02 :境外充值
        map.put("tradeCode","02");
        map.put("orderStatus", Arrays.asList("S","RS"));

        IPage<DepositOrderVO> pageCond = new Page<>(dto.getPage(), dto.getLimit());
        List<DepositOrderVO> depositOrderVOS = baseMapper.selectExList(pageCond,map);
        BigDecimal sum = BigDecimal.ZERO;
        if(CollectionUtils.isNotEmpty(depositOrderVOS)){
            for(DepositOrderVO depositOrderVO : depositOrderVOS){
                String payDate = depositOrderVO.getPayDate();
                String payTime = depositOrderVO.getPayTime();
                depositOrderVO.setPayDateTime(payDate + " " + payTime);
                if(OrderStatus.SUCCESS.getId().equals(depositOrderVO.getOrderStatus())){
                    sum = sum.add(depositOrderVO.getPrice());
                }
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("total", pageCond.getTotal());
        data.put("items", depositOrderVOS);
        data.put("sum", sum.doubleValue());
        if(StringUtils.isBlank(dto.getPayNo()) && StringUtils.isBlank(dto.getMobile()) && StringUtils.isBlank(dto.getName()) &&
            StringUtils.isBlank(dto.getUserId()) && StringUtils.isBlank(dto.getEndPayDate()) && StringUtils.isBlank(dto.getStartPayDate())){
            data.put("sum", baseMapper.sumExList());
        }

        return ResponseUtil.ok(data);
    }

    @Override
    public Map<String, Object> exList(MobileDepositOrderDTO depositOrderDTO) {
        if(StringUtils.isBlank(depositOrderDTO.getOrderStatus())){
            depositOrderDTO.setOrderStatus(OrderStatus.SUCCESS.getId());
        }
        IPage page = new Page(depositOrderDTO.getPage(), depositOrderDTO.getLimit());
        Map<String, Object> paramMap = BeanUtils.beanToMap(depositOrderDTO);
        List<DepositOrderVO> depositOrderVOS = baseMapper.selectDepositOrderListOfShop(page, paramMap);
        depositOrderVOS.forEach( depositOrderVO -> {
            String payDate = depositOrderVO.getPayDate();
            String payTime = depositOrderVO.getPayTime();
            depositOrderVO.setPayDateTime(payDate + " " + payTime);
        });
        Map<String, Object> data = new HashMap<>();
        data.put("total", page.getTotal());
        data.put("items", depositOrderVOS);
        return data;
    }

    @Override
    public StatDepositOrderVo statDepositOrder(Map<String, Object> params) {
        StatDepositOrderVo vo = baseMapper.statDepositOrder(params);
        if (null == vo) {
            vo = new StatDepositOrderVo();
        }
        return vo;
    }

    @Override
    public List<StatDepositOrderGroupByVo> statDepositOrderGroupBy(Map<String, Object> params) {
        return baseMapper.statDepositOrderGroupBy(params);
    }


    public Map<String, Object> buildParams(AdminDepositOrderDTO dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("user_id", StringUtils.isEmpty(dto.getUserId())?null:dto.getUserId());
        map.put("pay_no",StringUtils.isEmpty(dto.getPayNo())?null:dto.getPayNo());
        String endPayDate = dto.getEndPayDate();
        String startPayDate = dto.getStartPayDate();
        if (StringUtils.isEmpty(endPayDate)) {
            endPayDate = DateUtil.formatTimestamp2String(new Date(), DateUtil.DATE_PATTERN);
        }
        //三个月前
        if (StringUtils.isEmpty(startPayDate)) {
            startPayDate = DateUtil.formatTimestamp2String(DateUtil.dateAddMonths(new Date(), -3), DateUtil.DATE_PATTERN);
        }
        map.put("endPayDate", endPayDate);
        map.put("startPayDate", startPayDate);
        map.put("limitStart", (dto.getPage()-1) * dto.getLimit());
        if (dto.getPage().intValue() <= 1||!StringUtils.isEmpty(dto.getPayNo())) {
            map.put("limitStart", 0);
        }
        map.put("limitEnd", dto.getLimit());
        map.put("busi_type","06"); //06 :会员套餐
        map.put("order_status","S"); //W: 充值成功
        return map;
    }

    @Override
    public void exportExcel(AdminDepositOrderDTO dto, HttpServletResponse response, HttpServletRequest request) throws Exception {
        Map<String, Object> map = buildParams(dto);
        //总记录数
        QueryWrapper<DepositOrder> ge = new QueryWrapper<DepositOrder>().le("order_date", map.get("endPayDate")).ge("order_date", map.get("startPayDate"));
        ge.eq("order_status", map.get("order_status"));
        ge.eq("busi_type", map.get("busi_type"));
        if (null!=map.get("user_id")) {
            ge.eq("user_id", map.get("user_id"));
        }
        if (null!=map.get("pay_no")) {
            ge.eq("pay_no", map.get("pay_no"));
        }
        int count = count(ge);
        map.put("limitStart",0);
        map.put("limitStart",count);
        List<DepositOrderVO> depositOrderVOS = baseMapper.selectDepositOrderList(map);
        depositOrderVOS.forEach( depositOrderVO -> {
            String payDate = depositOrderVO.getPayDate();
            String payTime = depositOrderVO.getPayTime();
            depositOrderVO.setPayDateTime(payDate + " " + payTime);
        });
        ReportExcel reportExcel = new ReportExcel();
        reportExcel.excelExport(depositOrderVOS, DateUtil.toyyyy_MM_dd(LocalDateTime.now()), DepositOrderVO.class, 1, response, request);
    }

    @Override
    public String finishDepositToAdjust(AdjustAccount adjustAccount) {

//        BigDecimal price = adjustAccount.getPrice();
//        if (adjustAccount.getOperateType().equals(0)) {
//            price = price.negate();
//        }
//        if (adjustAccount.getOrderType().equals(1)) {// 充值
//            if (iUserIncomeService.updateTrainIncomeByModeTypeNUid(adjustAccount.getUserId(), price, 2) == 0) {
//                throw new ShopException("余额不足（编号：9）");
//            }
//            log.info("调账成功 - 9：{},{},{}", adjustAccount.getUserId(), adjustAccount.getUserId(), adjustAccount.getPrice());
//            return "9:9";
//        } else {// 订单
//
//            List<OrderGoods> orderGoodsList = iOrderGoodsService.list(new QueryWrapper<OrderGoods>().eq("order_no", adjustAccount.getOrderNo()));
//            Set<String> collect = orderGoodsList.stream().map(x -> x.getGoodsType() + ":" + x.getBuyerPartner()).collect(Collectors.toSet());
//
//            if (collect.contains("1:1")) {// 名品课时费 （600）
//                if (iUserIncomeService.updateTrainIncomeByModeTypeNUid(adjustAccount.getUserId(), price, 1) == 0) {
//                    throw new ShopException("余额不足（编号：6）");
//                }
//                log.info("调账成功 - 6：{},{},{}", adjustAccount.getUserId(), adjustAccount.getUserId(), adjustAccount.getPrice());
//                return "1:1";
//            } else if (collect.contains("1:0")) {// 基础课时收益 （399）
//                if (iUserIncomeService.updateTrainIncomeByModeTypeNUid(adjustAccount.getUserId(), price, 0) == 0) {
//                    throw new ShopException("余额不足（编号：3）");
//                }
//                log.info("调账成功 - 3：{},{},{}", adjustAccount.getUserId(), adjustAccount.getUserId(), adjustAccount.getPrice());
//                return "1:0";
//            } else if (collect.contains("0:0")) {// 普通订单
//                if (iUserIncomeService.updateTotalIncomeByUid(adjustAccount.getUserId(), price) == 0) {
//                    throw new ShopException("余额不足（编号：1）");
//                }
//                log.info("调账成功 - 0：{},{},{}", adjustAccount.getUserId(), adjustAccount.getUserId(), adjustAccount.getPrice());
//                return "0:0";
//            } else {
//                throw new ShopException("收益订单类型异常");
//            }
//        }
    	return null;
    }

    /**
     * 校验保证金余额
     *
     * @param depositOrder
     * @return true:余额充足. false:余额不足
     */
    @Override
    public boolean checkMarginBalance(DepositOrder depositOrder) {
        String orderNo = depositOrder.getOrderNo();
        String depositNo = depositOrder.getDepositNo();
        log.info("|保证金余额校验|开始|订单号:{},充值订单号:{}", orderNo, depositNo);
        Long userId = depositOrder.getUserId();
        BigDecimal price = depositOrder.getPrice();
        JSONObject bal = iCashService.balance(userId);
        if (bal == null) {
            log.info("|保证金余额校验|获取保证金余额失败, 用户id:{}, 订单号:{},充值订单号:{}", userId, orderNo, depositNo);
            return true;
        }
        BigDecimal sctBal = bal.getBigDecimal("sctBal");
        if (price.compareTo(sctBal) < 1) {
            return true;
        }
        log.info("|保证金余额校验|超出保证金余额, 用户id:{}, 用户状态:{}, 授权号:{},扣减金额:{}, 保证金余额:{}", userId, orderNo, depositNo, price, sctBal);
        // 充值订单表
        depositOrder.setOrderStatus(ConstantsEnum.DEPOSIT_ORDER_STATUS_F.stringValue());
        updateById(depositOrder);
        // 收益明细表
        iUserIncomeDetailsService.update(new UserIncomeDetails(),
                new UpdateWrapper<UserIncomeDetails>().set("status", UserIncomeStatusType.EXP.getId()).eq("source_order_no", depositNo));
        return false;
    }

    @Override
    public DepositOrder existOne(DepositOrder depositOrder) {

        QueryWrapper<DepositOrder> depositOrderQueryWrapper = new QueryWrapper<>();
        Optional.ofNullable(depositOrder.getOrderType()).ifPresent(x -> depositOrderQueryWrapper.eq("order_type", x));
        Optional.ofNullable(StringUtils.stripToNull(depositOrder.getOrderStatus())).ifPresent(x -> depositOrderQueryWrapper.eq("order_status", x));
        Optional.ofNullable(StringUtils.stripToNull(depositOrder.getBusiType())).ifPresent(x -> depositOrderQueryWrapper.eq("busi_type", x));
        Optional.ofNullable(StringUtils.stripToNull(depositOrder.getTradeCode())).ifPresent(x -> depositOrderQueryWrapper.eq("trade_code", x));

        return getOne(depositOrderQueryWrapper);
    }

    /**
     * 同步数据到php
     *
     * @param profitOrderMap    depositNo 充值订单号
     *                          orderType 订单类型
     *                          typeSplit 类型拆分
     */
    @Override
    public void syncDepositOrderToProfit(Map<String, Object> profitOrderMap) {
        log.info("同步充值订单至php:{}", profitOrderMap);

        // 推送至mq
        profitOrderMap.put("uuid", UUID.randomUUID());
        iRabbitMQSenderService.send(RabbitMQSenderImpl.SHOP_PROFIT_ORDER, profitOrderMap);

        // 保存数据到MqMessage
        iMqMessageService.n2Save(0, queueConfig.getExchange(), JSON.toJSONString(profitOrderMap),
                queueConfig.getQueues().getOrderProfit().getRoutingKey(), profitOrderMap.get("uuid").toString());

    }

}
