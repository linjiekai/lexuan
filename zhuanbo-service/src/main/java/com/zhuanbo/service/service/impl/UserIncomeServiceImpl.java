package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.Align;
import com.zhuanbo.core.constants.BuyTypeEnum;
import com.zhuanbo.core.constants.ChangeType;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.IncomeStatTypeEnum;
import com.zhuanbo.core.constants.IncomeType;
import com.zhuanbo.core.constants.OrderStatus;
import com.zhuanbo.core.constants.PlatformType;
import com.zhuanbo.core.constants.TradeCode;
import com.zhuanbo.core.constants.UserIncomeOperateType;
import com.zhuanbo.core.constants.UserIncomeStatusType;
import com.zhuanbo.core.constants.UserPointDetailsStatusEnum;
import com.zhuanbo.core.dto.AdminPointDTO;
import com.zhuanbo.core.dto.AdminUserIncomeDTO;
import com.zhuanbo.core.entity.DepositOrder;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.OrderGoods;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserIncome;
import com.zhuanbo.core.entity.UserIncomeDetails;
import com.zhuanbo.core.entity.UserIncomeSyn;
import com.zhuanbo.core.entity.UserPointDetails;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.service.mapper.UserIncomeMapper;
import com.zhuanbo.service.service.IAdminDealerService;
import com.zhuanbo.service.service.IDepositOrderService;
import com.zhuanbo.service.service.IOrderGoodsService;
import com.zhuanbo.service.service.IOrderService;
import com.zhuanbo.service.service.ISeqIncrService;
import com.zhuanbo.service.service.IUserIncomeDetailsService;
import com.zhuanbo.service.service.IUserIncomeService;
import com.zhuanbo.service.service.IUserPointDetailsService;
import com.zhuanbo.service.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 用户收益表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class UserIncomeServiceImpl extends ServiceImpl<UserIncomeMapper, UserIncome> implements IUserIncomeService {

	@Autowired
	private IUserIncomeDetailsService userIncomeDetailsService;

	@Autowired
	private IDepositOrderService depositOrderService;

	@Autowired
	private IOrderService orderService;

	@Autowired
	private IUserService iUserService;

	@Autowired
	private IOrderGoodsService iOrderGoodsService;

	@Autowired
	private IUserPointDetailsService iUserPointDetailsService;

	@Autowired
	private AuthConfig authConfig;

	@Autowired
	private ISeqIncrService iSeqIncrService;

	@Autowired
	private IDepositOrderService iDepositOrderService;

	@Autowired
	private IAdminDealerService iAdminDealerService;

	@Override
	public void goodsOrder(Order order, UserIncomeSyn userIncomeSyn, Integer incomeType, Integer changeType, String content, Long adjustUserId) {
		// 公司账号不处理
		if (Constants.COMPANY_USERID.longValue() == userIncomeSyn.getProfitUserId()) {
			return;
		}
		// 扣减累计收益和在途收益
		if (UserIncomeOperateType.ADD.getId() == userIncomeSyn.getOperateType()) {
			baseMapper.addTotalAndUavaAndShare(userIncomeSyn.getProfitUserId(), userIncomeSyn.getProfitAmount());
		} else {
			baseMapper.subtractTotalAndUavaAndShare(userIncomeSyn.getProfitUserId(), userIncomeSyn.getProfitAmount());
		}
		OrderGoods goods = iOrderGoodsService.getOne(new QueryWrapper<OrderGoods>().select("sum(price * number) price").eq("order_no", order.getOrderNo()));
		// 记录收益明细
		Long orderUserId = order.getUserId();
		if(ChangeType.ADJUST.getId() == changeType){
			orderUserId = adjustUserId;
		}else{
			if (userIncomeSyn.getIncomeType() != ConstantsEnum.INCOME_TYPE_6.integerValue()) {
				content = content + "￥" + goods.getPrice();
    		}
		}
		User orderUser = iUserService.getById(orderUserId);
		UserIncomeDetails details = new UserIncomeDetails();
		details.setUserId(userIncomeSyn.getProfitUserId());
		details.setOrderNo(order.getOrderNo());
		details.setChangeType(changeType);
		details.setRewardType(userIncomeSyn.getRewardType());
		details.setPrice(goods.getPrice());
		details.setFromUserId(orderUserId);
		details.setFromPtLevel(orderUser.getPtLevel());
		details.setSourceOrderNo(order.getSourceOrderNo());
		details.setAdjustNo(order.getAdjustNo());
		details.setOperateIncome(userIncomeSyn.getProfitAmount());
		details.setUsableIncome(userIncomeSyn.getProfitAmount());
		details.setOperateType(userIncomeSyn.getOperateType());
		details.setIncomeType(incomeType);
		details.setIncomeDate(DateUtil.date10());
		details.setIncomeTime(DateUtil.time8());
		details.setContent(content);
		userIncomeDetailsService.save(details);
	}

	@Override
	public void depositOrder(DepositOrder depositOrder, UserIncomeSyn userIncomeSyn, Integer incomeType, Integer changeType,
			String content, Long adjustUserId) throws Exception {
		// 公司账号不处理
		if (Constants.COMPANY_USERID.longValue() == userIncomeSyn.getProfitUserId()) {
			return;
		}
		// 扣减累计收益和在途收益
		if (UserIncomeOperateType.ADD.getId() == userIncomeSyn.getOperateType()) {
			baseMapper.addTotalAndUavaAndTrain(userIncomeSyn.getProfitUserId(), userIncomeSyn.getProfitAmount());
		} else {
			baseMapper.subtractTotalAndUavaAndTrain(userIncomeSyn.getProfitUserId(), userIncomeSyn.getProfitAmount());
		}

		// 记录收益明细
		Long orderUserId = depositOrder.getUserId();
		if(ChangeType.ADJUST.getId() == changeType){
			orderUserId = adjustUserId;
		}else{
			if (depositOrder.getPrice().doubleValue() > 0) {
				content = content + "￥" + depositOrder.getPrice();
			}
		}
		User orderUser = iUserService.getById(orderUserId);
		UserIncomeDetails details = new UserIncomeDetails();
		details.setUserId(userIncomeSyn.getProfitUserId());
		details.setOrderNo(depositOrder.getOrderNo());
		details.setChangeType(changeType);
		details.setRewardType(userIncomeSyn.getRewardType());
		details.setPrice(depositOrder.getPrice());
		details.setFromUserId(orderUserId);
		details.setFromPtLevel(orderUser.getPtLevel());
		details.setSourceOrderNo(depositOrder.getSourceOrderNo());
		details.setAdjustNo(depositOrder.getAdjustNo());
		details.setOperateIncome(userIncomeSyn.getProfitAmount());
		details.setUsableIncome(userIncomeSyn.getProfitAmount());
		details.setOperateType(userIncomeSyn.getOperateType());
		details.setIncomeType(incomeType);
		details.setIncomeDate(DateUtil.date10());
		details.setIncomeTime(DateUtil.time8());
		details.setContent(content);

		String tradeCode = TradeCode.SPECIALTRADE.getId();
		String busiType = ConstantsEnum.DEPOSIT_BUSI_TYPE_04.stringValue();

		if (userIncomeSyn.getOperateType() == UserIncomeOperateType.SUBSTRACT.getId()) {
			busiType = ConstantsEnum.DEPOSIT_BUSI_TYPE_07.stringValue();
		}

		if (ChangeType.ADJUST.getId() == changeType) {
			tradeCode = TradeCode.ADJUSTMENT.getId();
		}

		DepositOrder depOrder = depositOrderService.saveFromIncomeDetails(details, tradeCode, busiType, "MPPAY");
		details.setSourceOrderNo(depOrder.getDepositNo());
		userIncomeDetailsService.save(details);
	}

	@Override
	public void withdrOrder(Order order, Long userId, BigDecimal operateIncome, Integer operateType, Integer changeType,
			String content) throws Exception {
		// 公司账号不处理
		if (Constants.COMPANY_USERID.longValue() == order.getUserId()) {
			return;
		}
		// 记录收益
		UserIncomeDetails details = new UserIncomeDetails();
		details.setUserId(order.getUserId());
		details.setOrderNo(order.getOrderNo());
		details.setChangeType(changeType);
		details.setPrice(order.getPrice());
		details.setSourceOrderNo(order.getSourceOrderNo());
		details.setOperateIncome(operateIncome);
		details.setUsableIncome(operateIncome);
		details.setOperateType(operateType);
		details.setIncomeType(IncomeType.WITHDR.getId());
		details.setBankCode(order.getBankCode());
		details.setBankCardNo(order.getBankCardNo());
		details.setBankCardName(order.getBankCardName());
		details.setIncomeDate(DateUtil.date10());
		details.setIncomeTime(DateUtil.time8());
		details.setContent(content);
		userIncomeDetailsService.save(details);
	}

	@Override
	public void orderRefund(UserIncomeDetails userIncomeDetails, String content) throws Exception {
		// 公司账号不处理
		if (Constants.COMPANY_USERID.longValue() == userIncomeDetails.getUserId()) {
			return;
		}
		// 判断收益明细状态:如果已退款
		String orderNo = userIncomeDetails.getOrderNo();
		Integer status = userIncomeDetails.getStatus();
		if (UserIncomeStatusType.EXP.getId() == status) {
			log.info("|收益退款|原订单已取消|订单号:{}", orderNo);
			return;
		}
		// 退款:收益扣减
		BigDecimal operateIncome = userIncomeDetails.getOperateIncome();
		Long userId = userIncomeDetails.getUserId();
		
		if (userIncomeDetails.getOperateType() == UserIncomeOperateType.ADD.getId()) {
			baseMapper.subtractTotalAndUavaAndShare(userId, operateIncome);
		} else {
			baseMapper.addTotalAndUavaAndShare(userId, operateIncome);
		}
		
		// 校验订单是否统计
		Integer statType = userIncomeDetails.getStatType();
		if (IncomeStatTypeEnum.NO.getId() == statType || StringUtils.isBlank(userIncomeDetails.getSourceOrderNo())) {
			userIncomeDetails.setStatus(UserIncomeStatusType.EXP.getId());
			userIncomeDetailsService.updateById(userIncomeDetails);
			log.info("|收益退款|原订单统计类型:{}|订单号:{}", statType, orderNo);
			return;
		}

		// 充值订单
		String tradeCode = TradeCode.SPECIALREFUND.getId();
		Integer operateType = UserIncomeOperateType.SUBSTRACT.getId();
		String busiType = ConstantsEnum.DEPOSIT_BUSI_TYPE_07.stringValue();
		if (UserIncomeOperateType.SUBSTRACT.getId() == userIncomeDetails.getOperateType()) {
			operateType = UserIncomeOperateType.ADD.getId();
			busiType = ConstantsEnum.DEPOSIT_BUSI_TYPE_04.stringValue();
		}

		// 记录收益明细
		Long fromUserId = userIncomeDetails.getFromUserId();
		User fromUser = iUserService.getById(fromUserId);
		UserIncomeDetails details = new UserIncomeDetails();
		details.setUserId(userId);
		details.setOrderNo(orderNo);
		details.setChangeType(ChangeType.REFUND.getId());
		details.setRewardType(userIncomeDetails.getRewardType());
		details.setPrice(userIncomeDetails.getPrice());
		details.setFromUserId(fromUserId);
		details.setFromPtLevel(fromUser.getPtLevel());
		details.setOperateIncome(operateIncome);
		details.setUsableIncome(userIncomeDetails.getUsableIncome());
		details.setOperateType(operateType);
		details.setIncomeType(userIncomeDetails.getIncomeType());
		details.setIncomeDate(DateUtil.date10());
		details.setIncomeTime(DateUtil.time8());
		details.setContent(content + "￥" + userIncomeDetails.getPrice());

		DepositOrder depOrder = depositOrderService.saveFromIncomeDetails(details, tradeCode, busiType, "MPPAY");

		details.setSourceOrderNo(depOrder.getDepositNo());
		userIncomeDetailsService.save(details);
	}

	@Override
	public UserIncome makeUserIncome(Long userId) {

		UserIncome userIncome = getOne(new QueryWrapper<UserIncome>().eq("user_id", userId));
		if (userIncome != null) {
			return userIncome;
		}
		LocalDateTime now = LocalDateTime.now();
		userIncome = new UserIncome();
		userIncome.setUserId(userId);
		userIncome.setLastTime(System.currentTimeMillis());
		userIncome.setAddTime(now);
		userIncome.setUpdateTime(now);
		userIncome.setTotalTeam(1);
		save(userIncome);
		return getOne(new QueryWrapper<UserIncome>().eq("user_id", userId));
	}

	@Override
	public void income2Deposit(Long userId, List<Integer> changeTypes) throws Exception {
		
		QueryWrapper<UserIncomeDetails> queryWrapper = new QueryWrapper<UserIncomeDetails>().eq("user_id", userId)
				.eq("stat_type", 0).eq("source_order_no", "").in("income_type", 1, 2, 3, 5, 6)
				.eq("status", UserIncomeStatusType.NORMAL.getId());
		if (changeTypes != null && changeTypes.size() > 0) {
			queryWrapper.in("change_type", changeTypes);
		}
		List<UserIncomeDetails> list = userIncomeDetailsService.list(queryWrapper);

		DepositOrder depositOrder = null;

		Order order = null;

		log.info("在途收益转可提收益,用户id:{},在途收益数量:{}", userId, list.size());
		for (UserIncomeDetails userIncomeDetails : list) {

			order = orderService.getOne(new QueryWrapper<Order>().eq("order_no", userIncomeDetails.getOrderNo()));
			if (null == order) {
				log.info("在途收益转可提收益,收益明细订单不存在orderNo={}", userIncomeDetails.getOrderNo());
				continue;
			}
			
			if (!(order.getOrderStatus().equals(OrderStatus.SUCCESS.getId()) || order.getOrderStatus().equals(OrderStatus.WAIT_DELIVER.getId()))
					&& ChangeType.ADJUST.getId() != userIncomeDetails.getChangeType()) {
				log.info("在途收益转可提收益,用户ID:{}, 订单变更类型({}),账本类型({}),订单({})状态未成功,订单状态:{}", userIncomeDetails.getChangeType(), userIncomeDetails.getChangeType(),
						userIncomeDetails.getIncomeType(), userIncomeDetails.getOrderNo(), order.getOrderStatus());
				continue;
			}
			
			// 转充值记录，定时转余额
			String tradeCode = TradeCode.SPECIALTRADE.getId();
			String busiType = ConstantsEnum.DEPOSIT_BUSI_TYPE_04.stringValue();

			if (userIncomeDetails.getChangeType() == ChangeType.ADJUST.getId()) {
				tradeCode = TradeCode.ADJUSTMENT.getId();
			} else if (userIncomeDetails.getChangeType() == ChangeType.REFUND.getId()) {
				tradeCode = TradeCode.SPECIALREFUND.getId();
			}

			if (userIncomeDetails.getOperateType() == UserIncomeOperateType.SUBSTRACT.getId()) {
				busiType = ConstantsEnum.DEPOSIT_BUSI_TYPE_07.stringValue();
			}

			depositOrder = depositOrderService.saveFromIncomeDetails(userIncomeDetails, tradeCode, busiType, "MPPAY");
			userIncomeDetails.setSourceOrderNo(depositOrder.getDepositNo());

			log.info("在途收益转可提收益,用户id:{},orderNo:{}, depositNo:{}", userIncomeDetails.getOrderNo(), depositOrder.getDepositNo());
			
			userIncomeDetailsService.updateById(userIncomeDetails);
		}
	}

	@Override
	public boolean subtractUavaIncome(Long userId, BigDecimal uavaIncome) {
		return ServiceImpl.retBool(baseMapper.subtractUavaIncome(userId, uavaIncome));
	}

	@Override
	public boolean addUavaIncome(Long userId, BigDecimal price) {
		return ServiceImpl.retBool(baseMapper.addUavaIncome(userId, price));
	}

	/**
	 * 根据用户id查询
	 * @param userId
	 * @return
	 */
	@Override
	public UserIncome getByUserId(Long userId){
		return this.getOne(new QueryWrapper<UserIncome>().eq("user_id", userId));
	}

	/**
	 * 积分充值
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void depositPoint(AdminPointDTO adminPointDTO) {
		log.info("|用户积分充值|接受到请求报文:{}", adminPointDTO);
		Long userId = adminPointDTO.getUserId();
		Long fromUserId = adminPointDTO.getFromUserId();
		Integer point = adminPointDTO.getPoint();
		Integer pointType = adminPointDTO.getPointType();
		Integer operateType = adminPointDTO.getOperateType();
		String tradeCode = adminPointDTO.getTradeCode();
		String busiType = adminPointDTO.getBusiType();
		Long adminId = adminPointDTO.getAdminId();
		Integer ptLevel = adminPointDTO.getPtLevel();
		List<Integer> typeSplit = adminPointDTO.getTypeSplit();
		String remark = adminPointDTO.getRemark();
		// 新增总积分/可用积分
		if (UserIncomeOperateType.ADD.getId() == operateType) {
			boolean addFalg = this.addTotalAndUsablePoint(userId, point);
			if (!addFalg) {
				throw new ShopException(10068);
			}
		} else if(UserIncomeOperateType.SUBSTRACT.getId() == operateType) {
			boolean subtractFlag = this.subtractUsablePoint(userId, point);
			if (!subtractFlag) {
				throw new ShopException(10069);
			}
		}
		UserIncome userIncome = this.getByUserId(userId);

		// 记录积分详情
		User operateUser = iUserService.getById(adminId);
		String orderNo = DateUtil.date8() + iSeqIncrService.nextVal("order_no", 8, Align.LEFT);
		adminPointDTO.setOrderNo(orderNo);
		UserPointDetails pointDetails = new UserPointDetails();
		pointDetails.setUserId(userId);
		pointDetails.setOrderNo(orderNo);
		pointDetails.setPlatform(PlatformType.ZBMALL.getCode());
		pointDetails.setOperatePoint(point);
		pointDetails.setUsablePoint(userIncome.getUsablePoint());
		pointDetails.setOperateType(operateType);
		pointDetails.setPointType(pointType);
		pointDetails.setStatus(UserPointDetailsStatusEnum.EFFECTIVE.getId());
		pointDetails.setPointDate(LocalDate.now().toString());
		pointDetails.setPointTime(DateUtil.time8());
		pointDetails.setFromUserId(fromUserId);
		pointDetails.setOperatorId(adminId);
		pointDetails.setRemark(remark);
		if (TradeCode.DEPOSIT.getId().equals(tradeCode)) {
			// 充值用户未 admin_dealer 的用户
			String adminName = iAdminDealerService.getAdminName(adminId.intValue());
			pointDetails.setOperator(adminName);
		} else {
			if (operateUser != null) {
				pointDetails.setOperator(operateUser.getName());
			}
		}
		iUserPointDetailsService.save(pointDetails);

		if (TradeCode.DEPOSIT.getId().equals(tradeCode)) {
			// 充值积分不记录充值订单表
			return;
		}
		// 记录充值订单
		User user = iUserService.getById(fromUserId);
		LocalDateTime now = LocalDateTime.now();
		DepositOrder depositOrder = new DepositOrder();
		depositOrder.setMercId(authConfig.getMercId());
		depositOrder.setPlatform(ConstantsEnum.PLATFORM_ZBMALL.stringValue());
		depositOrder.setOrderNo(orderNo);
		depositOrder.setDepositNo(DateUtil.date8() + iSeqIncrService.nextVal("order_no", 8, Align.LEFT));
		adminPointDTO.setDepositNo(depositOrder.getDepositNo());
		depositOrder.setOrderDate(DateUtil.toyyyy_MM_dd(now));
		depositOrder.setOrderTime(DateUtil.toHH_mm_ss(now));
		depositOrder.setBusiType(busiType);
		depositOrder.setTradeCode(tradeCode);
		depositOrder.setBuyType(BuyTypeEnum.BUY_TYPE_3.value());
		depositOrder.setUserId(fromUserId);
		depositOrder.setClientIp("127.0.0.1");
		depositOrder.setMobile(user == null ? null : user.getMobile());
		depositOrder.setOrderStatus(ConstantsEnum.DEPOSIT_ORDER_STATUS_S.stringValue());
		depositOrder.setInviteCode(operateUser.getInviteCode());
		depositOrder.setTypeSplit(typeSplit);
		depositOrder.setUpgradeLevel(ptLevel);
		Optional<Integer> max = typeSplit.stream().max(Comparator.naturalOrder());
		depositOrder.setOrderType(max.get());
		iDepositOrderService.save(depositOrder);
	}

	/**
	 * 增加总积分和可用积分
	 *
	 * @param userId
	 * @param point
	 */
	@Override
	public boolean addTotalAndUsablePoint(Long userId, Integer point) {
		return ServiceImpl.retBool(baseMapper.addTotalAndUsablePoint(userId, point));
	}

	/**
	 * 积分扣减(扣减可用积分,增加已用积分)
	 *
	 * @param userId
	 * @param point
	 * @throws Exception
	 */
	@Override
	public boolean subtractUsablePoint(Long userId, Integer point) {
		return ServiceImpl.retBool(baseMapper.subtractUsablePoint(userId, point));
	}

	/**
	 * 获取积分信息
	 *
	 * @param adminPointDTO
	 * @return
	 */
	@Override
	public List<AdminUserIncomeDTO> pagePointInfo(IPage iPage, AdminPointDTO adminPointDTO) {
		List<AdminUserIncomeDTO> userIncomeDTOS = baseMapper.pagePointInfo(iPage, adminPointDTO);
		userIncomeDTOS.forEach(incomeDTO -> {
			Long userId = incomeDTO.getUserId();
			List<UserPointDetails> details = iUserPointDetailsService.list(new QueryWrapper<UserPointDetails>().eq("user_id", userId).orderByDesc("add_time"));
			if (details != null && details.size() > 0) {

				UserPointDetails userPointDetails = details.get(0);
				incomeDTO.setRemark(userPointDetails.getRemark());
			}

		});
		return userIncomeDTOS;
	}

	/**
	 * 扣减积分校验
	 *
	 * @param userId
	 * @param point
	 * @return
	 */
	@Override
	public boolean checkSubtracPoint(Long userId, Integer point) {
		UserIncome userIncome = this.getOne(new QueryWrapper<UserIncome>().eq("user_id", userId));
		Integer usablePoint = userIncome.getUsablePoint();
		if (point > usablePoint) {
			return false;
		}
		return true;
	}

}
