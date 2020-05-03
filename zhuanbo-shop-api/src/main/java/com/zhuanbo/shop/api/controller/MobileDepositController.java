package com.zhuanbo.shop.api.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginUser;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.config.QueueConfig;
import com.zhuanbo.core.constants.Align;
import com.zhuanbo.core.constants.BuyTypeEnum;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.PtLevelType;
import com.zhuanbo.core.dto.BuyInviteCodeDTO;
import com.zhuanbo.core.dto.MobileDepositOrderDTO;
import com.zhuanbo.core.entity.DepositOrder;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.util.MliveClientUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.vo.BuyInviteCodeCheckResultVO;
import com.zhuanbo.service.service.ICashService;
import com.zhuanbo.service.service.IDepositOrderService;
import com.zhuanbo.service.service.IDictionaryService;
import com.zhuanbo.service.service.IMqMessageService;
import com.zhuanbo.service.service.ISeqIncrService;
import com.zhuanbo.service.service.IUserBuyInviteCodeService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.service3rd.rabbitmq.IRabbitMQSenderService;
import com.zhuanbo.service.service3rd.rabbitmq.impl.RabbitMQSenderImpl;
import com.zhuanbo.shop.api.dto.req.DepositDTO;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/shop/mobile/deposit")
@Slf4j
public class MobileDepositController {

	@Value("${deposit-pay-notify-url}")
	private String depositPayNotifyUrl;

	@Autowired
	private AuthConfig authConfig;
	@Autowired
	private ISeqIncrService iSeqIncrService;
	@Autowired
	private IDepositOrderService iDepositOrderService;
	@Autowired
	private IUserService iUserService;
	@Autowired
	private PlatformTransactionManager txManager;
	@Autowired
	private ICashService iCashService;
	@Autowired
	private IDictionaryService iDictionaryService;
	@Autowired
    private IMqMessageService iMqMessageService;
	@Autowired
    private QueueConfig queueConfig;
	@Autowired
	private IRabbitMQSenderService iRabbitMQSenderService;
	@Autowired
	private IUserBuyInviteCodeService iUserBuyInviteCodeService;
	

	@PostMapping("/submit")
	public Object submit(@LoginUser Long uid, @RequestBody DepositDTO depositDTO) {

		log.info("充值订单提交[{}]", JSON.toJSONString(depositDTO));
		User user = iUserService.getById(uid);
		if (null == user) {
			return ResponseUtil.fail(10007);
		}
		//下级用户
		User userDown = null;
		BigDecimal price = null;
		String orderStatus = ConstantsEnum.DEPOSIT_ORDER_STATUS_W.stringValue();
		
		List<Integer> typeSplit = new ArrayList<>();
		typeSplit.add(depositDTO.getOrderType());
		
		Integer upgradeLevel = depositDTO.getOrderType();
		
		if (ConstantsEnum.DEPOSIT_BUSI_TYPE_06.stringValue().equals(depositDTO.getBusiType())) {

			if (null == depositDTO.getOrderType() || depositDTO.getOrderType() < 1) {
				return ResponseUtil.fail();
			}
			
			if (null != depositDTO.getBuyType() && depositDTO.getBuyType()==BuyTypeEnum.BUY_TYPE_2.value()) {
				price = new BigDecimal(0);
				orderStatus = ConstantsEnum.DEPOSIT_ORDER_STATUS_S.stringValue();
				if (StringUtils.isBlank(depositDTO.getCouponSn())) {
					return ResponseUtil.fail(11111);
				}
				
				userDown = iUserService.getById(depositDTO.getUserId());
				
				if (null == userDown) {
					return ResponseUtil.fail(10007);
				}
				
			} else {
				
//				if (depositDTO.getOrderType() == 1) {
//					return ResponseUtil.result(10073, "该套餐已下架，请购买其他套餐");
//				}
				
				if (depositDTO.getOrderType() < 1) {
					return ResponseUtil.fail(10072);
				}
				
				if (depositDTO.getOrderType() > 3) {
					return ResponseUtil.fail(10076);
				}
				
				if (user.getPtLevel() >= PtLevelType.PARTNER.getId()) {
					return ResponseUtil.fail(10075);
				}
				
				if (!StringUtils.isBlank(depositDTO.getBuyCode())) {
					BuyInviteCodeDTO buyInviteCodeDTO = new BuyInviteCodeDTO();
					buyInviteCodeDTO.setUserId(user.getId());
					buyInviteCodeDTO.setBuyInviteCode(depositDTO.getBuyCode());
					BuyInviteCodeCheckResultVO buyInviteCodeCheckResultVO = iUserBuyInviteCodeService.checkCode(buyInviteCodeDTO);
					
					if (buyInviteCodeCheckResultVO.getOrderType() != depositDTO.getOrderType().intValue()) {
						return ResponseUtil.result(10072, "请购买对应级别套餐");
					}
					
					typeSplit = buyInviteCodeCheckResultVO.getOrderTypeList();
					depositDTO.setInviteCode(depositDTO.getBuyCode().substring(0, depositDTO.getBuyCode().length() - 2));
					price = buyInviteCodeCheckResultVO.getPrice();
					upgradeLevel = 0;
				} else {
					
					if (user.getPtLevel() == PtLevelType.ORDINARY.getId()) {
						return ResponseUtil.fail(10074, "抱歉，你暂无购买资格，请联系你的上级进行购买");
					}
					
					price = MliveClientUtil.investors(authConfig.getMliveAdminUrl(), "goodsType=3").getLevelPrice(depositDTO.getOrderType());
				}
				
			}
		} else if (ConstantsEnum.DEPOSIT_BUSI_TYPE_08.stringValue().equals(depositDTO.getBusiType())) {
			price = depositDTO.getPrice();
		} else {
			return ResponseUtil.fail();
		}
		if (depositDTO.equals(1) && StringUtils.isBlank(depositDTO.getInviteCode())) {
			return ResponseUtil.result(10053);
		}
		
		LocalDateTime now = LocalDateTime.now();
		DepositOrder depositOrder = new DepositOrder();
		depositOrder.setMercId(authConfig.getMercId());
		depositOrder.setPlatform(ConstantsEnum.PLATFORM_ZBMALL.stringValue());
		depositOrder.setDepositNo(DateUtil.date8() + iSeqIncrService.nextVal("order_no", 8, Align.LEFT));
		depositOrder.setOrderNo(depositOrder.getDepositNo());
		depositOrder.setOrderDate(DateUtil.toyyyy_MM_dd(now));
		depositOrder.setOrderTime(DateUtil.toHH_mm_ss(now));
		depositOrder.setTradeType(depositDTO.getTradeType());
		depositOrder.setTradeCode(depositDTO.getTradeCode());
		
		if (null != depositDTO.getBuyType() && depositDTO.getBuyType()==BuyTypeEnum.BUY_TYPE_2.value()) {
			//赠品订单充值登记被下单人userId
			depositOrder.setUserId(depositDTO.getUserId());
		} else {
			depositOrder.setUserId(uid);
		}
		
		log.info("充值订单depositNo[{}],请求参数[{}]", depositOrder.getDepositNo(), JSON.toJSONString(depositDTO));
		
		depositOrder.setPrice(price);
		depositOrder.setBusiType(depositDTO.getBusiType());
		depositOrder.setOrderStatus(orderStatus);
		depositOrder.setInviteCode(depositDTO.getInviteCode());
		depositOrder.setBuyCode(depositDTO.getBuyCode());
		depositOrder.setClientIp(depositDTO.getClientIp());
		depositOrder.setAddTime(now);
		depositOrder.setUpdateTime(now);
		depositOrder.setMobile(user.getMobile());
		depositOrder.setOrderType(depositDTO.getOrderType());
		depositOrder.setBuyType(depositDTO.getBuyType());
		depositOrder.setCouponSn(depositDTO.getCouponSn());
		depositOrder.setTypeSplit(typeSplit);
		depositOrder.setUpgradeLevel(upgradeLevel);
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		TransactionStatus transaction = null;

		try {
			transaction = txManager.getTransaction(def);
			iDepositOrderService.save(depositOrder);
			Map<String, Object> map = new HashMap<>();
			if (null != depositDTO.getBuyType() && depositDTO.getBuyType()!=BuyTypeEnum.BUY_TYPE_2.value()) {
				map.put("notifyUrl", depositPayNotifyUrl);
				map.put("mercId", authConfig.getMercId());
				map.put("orderNo", depositOrder.getDepositNo());
				map.put("orderDate", DateUtil.toyyyy_MM_dd(now));
				map.put("orderTime", DateUtil.toHH_mm_ss(now));
				map.put("price", price);
				map.put("sysCnl", depositDTO.getSysCnl());
				map.put("clientIp", depositDTO.getClientIp());
				map.put("platform", depositDTO.getPlatform());
				map.put("busiType", depositDTO.getBusiType());
				map.put("tradeCode", depositDTO.getTradeCode());
				String prePayNo = iCashService.prePay(user, map);
				if (StringUtils.isBlank(prePayNo)) {
					log.error("===充值押金失败的单号：{}", depositOrder.getDepositNo());
					throw new RuntimeException("充值押金失败");
				}
				map.clear();
				map.put("orderNo", depositOrder.getDepositNo());
				map.put("prePayNo", prePayNo);
				map.put("mercId", authConfig.getMercId());
			} else {
				
				map.put("orderNo", depositOrder.getDepositNo());
				map.put("mercId", authConfig.getMercId());
				map.put("orderType", depositOrder.getOrderType());
				map.put("couponSn", depositOrder.getCouponSn());
				
				Map<String, Object> mqMessageData = new HashMap<String, Object>();
				mqMessageData.put("orderType", depositDTO.getOrderType());
				mqMessageData.put("orderNo", depositOrder.getDepositNo());
				mqMessageData.put("typeSplit", typeSplit);
				mqMessageData.put("uuid", UUID.randomUUID().toString());
		        iMqMessageService.n2Save(0, queueConfig.getExchange(), JSON.toJSONString(mqMessageData),
		                queueConfig.getQueues().getOrderProfit().getRoutingKey(), mqMessageData.get("uuid").toString());
		        iRabbitMQSenderService.send(RabbitMQSenderImpl.SHOP_PROFIT_ORDER, mqMessageData);
			}
			txManager.commit(transaction);
			return ResponseUtil.ok(map);
		} catch (Exception e) {
			log.error("deposit:submit:error:{}", e);
			txManager.rollback(transaction);
		}
		return ResponseUtil.fail();
	}

	/**
	 * 站外充值记录
	 *
	 * @param uid
	 * @param depositOrderDTO
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/exList")
	public Object exList(@LoginUser Long uid, @RequestBody MobileDepositOrderDTO depositOrderDTO) throws Exception {
		depositOrderDTO.setUserId(uid);
		Map<String, Object> retData = iDepositOrderService.exList(depositOrderDTO);
		return ResponseUtil.ok(retData);
	}

	/**
	 * 分页查询充值订单
	 *
	 * @param uid
	 * @param depositOrderDTO
	 * @return
	 */
	@GetMapping("/page")
	public Object page(@LoginUser Long uid, @RequestBody MobileDepositOrderDTO depositOrderDTO) {
		log.info("|充值订单列表|分页查询|用户id：{}", uid);
		QueryWrapper<DepositOrder> queryWrapper = new QueryWrapper<>();
		IPage iPage = new Page(depositOrderDTO.getPage(), depositOrderDTO.getLimit());
		queryWrapper.eq("merc_id", depositOrderDTO.getMercId());
		queryWrapper.eq("user_id", uid);
		queryWrapper.orderByDesc("add_time");
		IPage depositOrderPage = iDepositOrderService.page(iPage, queryWrapper);
		Map<String, Object> data = new HashMap<>();
		data.put("total", depositOrderPage.getTotal());
		data.put("items", depositOrderPage.getRecords());
		return ResponseUtil.ok(data);
	}

	/**
	 * 获取充值订单
	 *
	 * @param uid
	 * @param depositOrderDTO
	 * @return
	 */
	@PostMapping("/get/info")
	public Object getInfo(@LoginUser Long uid, @RequestBody MobileDepositOrderDTO depositOrderDTO) {
		log.info("|获取充值订单信息|用户id:{},请求报文:{}", uid, depositOrderDTO);
		QueryWrapper<DepositOrder> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("merc_id", depositOrderDTO.getMercId());
		queryWrapper.eq("user_id", uid);
		queryWrapper.eq("deposit_no", depositOrderDTO.getDepositNo());
		DepositOrder depositOrder = iDepositOrderService.getOne(queryWrapper);
		return ResponseUtil.ok(depositOrder);
	}
}
