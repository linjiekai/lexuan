package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.constants.Align;
import com.zhuanbo.core.constants.ChangeType;
import com.zhuanbo.core.constants.IncomeAdjustCategoryEnum;
import com.zhuanbo.core.constants.IncomeAdjustOperateTypeEnum;
import com.zhuanbo.core.constants.IncomeAdjustOrderTypeEnum;
import com.zhuanbo.core.constants.UserIncomeOperateType;
import com.zhuanbo.core.entity.AdjustAccount;
import com.zhuanbo.core.entity.DepositOrder;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserIncomeDetails;
import com.zhuanbo.core.entity.UserIncomeSyn;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.service.mapper.AdjustAccountMapper;
import com.zhuanbo.service.service.IAdjustAccountService;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.IDepositOrderService;
import com.zhuanbo.service.service.IOrderService;
import com.zhuanbo.service.service.ISeqIncrService;
import com.zhuanbo.service.service.IUserIncomeDetailsService;
import com.zhuanbo.service.service.IUserIncomeService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.service3rd.rabbitmq.IRabbitMQSenderService;
import com.zhuanbo.service.service3rd.rabbitmq.impl.RabbitMQSenderImpl;
import com.zhuanbo.service.vo.AdjustAccountVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * 调怅记录表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-09-24
 */
@Service
@Slf4j
public class AdjustAccountServiceImpl extends ServiceImpl<AdjustAccountMapper, AdjustAccount> implements IAdjustAccountService {

	@Autowired
    private IAdminService iAdminService;
    @Autowired
    private ISeqIncrService iSeqIncrService;
    @Autowired
    private IOrderService iOrderService;
    @Autowired
    private IUserIncomeService iUserIncomeService;
    @Autowired
    private IRabbitMQSenderService iRabbitMQSenderService;
    @Autowired
    private IDepositOrderService iDepositOrderService;
    @Autowired
    private IUserIncomeDetailsService iUserIncomeDetailsService;
    @Autowired
    private IUserService iUserService;

    @Override
    public Page<AdjustAccountVO> list(Page<AdjustAccountVO> page, Map<String, Object> params) {

        List<AdjustAccountVO> list = baseMapper.list(page, params);
        page.setRecords(list);
        return page;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addOne(Integer adminId, AdjustAccount adjustAccount) throws Exception {
        log.info("新增调账,操作人:{},接收到请求报文:{}", adminId, adjustAccount);
        Long userId = adjustAccount.getUserId();
        Integer orderType = adjustAccount.getOrderType();
        Integer adjustType = adjustAccount.getAdjustType();
        Long adjustUserId = adjustAccount.getAdjustUserId();
        if (BigDecimal.ZERO.compareTo(adjustAccount.getPrice()) != -1) {
            log.error("新增调账,调账金额需大于零,请求参数:{}", adjustAccount);
            throw new ShopException(10059);
        }

        // 校验调账的原订单是否存在
        if (orderType == IncomeAdjustOrderTypeEnum.ORDER.getId()) {
            Order orgOrder = iOrderService.getOne(new QueryWrapper<Order>().eq("order_no", adjustAccount.getOrderNo()));
            Optional.ofNullable(orgOrder).orElseThrow(() -> {
                log.error("新增调账,原订单单号不存在,订单单号:{}", adjustAccount.getOrderNo());
                return new ShopException(10057);
            });
        } else if (orderType == IncomeAdjustOrderTypeEnum.DEPOSIT.getId()) {
            List<DepositOrder> depositOrderList = iDepositOrderService.list(new QueryWrapper<DepositOrder>().eq("order_no", adjustAccount.getOrderNo()));
            if (depositOrderList == null && depositOrderList.size() < 1) {
                log.error("新增调账,原充值订单单号不存在,订单单号:{}", adjustAccount.getOrderNo());
                throw new ShopException(10065);
            }
        }

        List<UserIncomeDetails> detailList = iUserIncomeDetailsService.list(new QueryWrapper<UserIncomeDetails>().eq("order_no", adjustAccount.getOrderNo()));
        boolean userIdFlag = detailList.stream().anyMatch(detail -> detail.getUserId().equals(userId));
        boolean fromUserIdFlag = detailList.stream().anyMatch(detail -> detail.getFromUserId().equals(userId));
        if (!userIdFlag && !fromUserIdFlag) {
            log.error("新增调账,原订单单号与当前用户无关联,订单号:{}", adjustAccount.getOrderNo());
            throw new ShopException(10061);
        }

        // 校验原订单是否进行调账  user_id, order_no, change_type, income_type
        UserIncomeDetails userIncomeDetails = iUserIncomeDetailsService.getOne(new QueryWrapper<UserIncomeDetails>().eq("user_id", userId).eq("order_no", adjustAccount.getOrderNo())
                .eq("change_type", ChangeType.ADJUST.getId()).eq("income_type", adjustType));
        if(userIncomeDetails != null){
            log.error("新增调账,原订单已进行调账,用户id:{},订单号:{},changeType:{},incomeType:{}", userId, adjustAccount.getOrderNo(), ChangeType.ADJUST.getId(), adjustType);
            throw new ShopException(10062);
        }

        // 校验用户是否存在
        User user = iUserService.getById(userId);
        User adjustUser = iUserService.getById(adjustUserId);
        Optional.ofNullable(user).orElseThrow(() -> {
            log.error("新增调账,被调账用户不存在");
            return new ShopException(10063);
        });
        Optional.ofNullable(adjustUser).orElseThrow(() -> {
            log.error("新增调账,调账发起人不存在");
            return new ShopException(10064);
        });

        // 保存调怅记录
        LocalDateTime now = LocalDateTime.now();
        String adjustNo = DateUtil.date8() + iSeqIncrService.nextVal("order_no", 8, Align.LEFT);
        adjustAccount.setAdjustNo(adjustNo);
        adjustAccount.setOperatorId(Long.valueOf(adminId));
        adjustAccount.setOperator(iAdminService.getAdminName(adminId));
        adjustAccount.setAddTime(now);
        adjustAccount.setUpdateTime(now);
        this.save(adjustAccount);

        // 收益详情处理
        BigDecimal price = adjustAccount.getPrice();
        String reason = adjustAccount.getReason();
        int operateType = IncomeAdjustOperateTypeEnum.SUBSTRACT.getId() == adjustAccount.getOperateType() ? UserIncomeOperateType.SUBSTRACT.getId() : UserIncomeOperateType.ADD.getId();

        UserIncomeSyn userIncomeSyn = new UserIncomeSyn();
        userIncomeSyn.setOrderNo(adjustAccount.getOrderNo());
    	userIncomeSyn.setProfitAmount(price);
    	userIncomeSyn.setProfitUserId(userId);
    	userIncomeSyn.setOperateType(operateType);
        switch (orderType){
            case 0:
                Order order = new Order();
                order.setOrderNo(adjustAccount.getOrderNo());
                order.setUserId(userId);
                order.setPrice(price);
                order.setAdjustNo(adjustNo);
                iUserIncomeService.goodsOrder(order, userIncomeSyn, adjustType, ChangeType.ADJUST.getId(), reason,adjustUserId);
                break;
            case 1:
                DepositOrder depositOrder = new DepositOrder();
                depositOrder.setOrderNo(adjustAccount.getOrderNo());
                depositOrder.setUserId(userId);
                depositOrder.setPrice(price);
                depositOrder.setAdjustNo(adjustNo);
                depositOrder.setDepositNo(adjustAccount.getAdjustNo());
            	iUserIncomeService.depositOrder(depositOrder, userIncomeSyn, adjustType, ChangeType.ADJUST.getId(), reason,adjustUserId);
                break;
            default:
                log.error("新增调账无效,订单类型:{},收益类型:{}",orderType,adjustType);
                throw new ShopException(10058);
        }

        if (operateType == UserIncomeOperateType.SUBSTRACT.getId()) {
            // 如果为扣减,调用收益转化
            iUserIncomeService.income2Deposit(userId, Arrays.asList(ChangeType.ADJUST.getId()));
        } else {
            // 转可提收益
            iRabbitMQSenderService.send(RabbitMQSenderImpl.INCOME_CHANGE_DEPOSIT, userId);
        }
    }

    /**
     * 手工调账
     *
     */
    @Override
    public Map<String, List<AdjustAccount>> manualAdd() {
        // 查询临时数据 shop_user_income_details_bak_20191221
        long flagTime = System.currentTimeMillis();
        long startTime = System.currentTimeMillis();
        log.info("|手工调账|开始|");
        List<Map<String, Object>> detailsMapList = baseMapper.listTemp();
        AdjustAccount adjustAccount;
        List<AdjustAccount> accountList = new ArrayList<>();
        for (Map<String, Object> detailsMap : detailsMapList) {
            adjustAccount = new AdjustAccount();
            adjustAccount.setUserId((Long)detailsMap.get("user_id"));
            adjustAccount.setOrderNo((String)detailsMap.get("order_no"));
            adjustAccount.setOrderType(IncomeAdjustOrderTypeEnum.ORDER.getId());
            adjustAccount.setAdjustCategory(IncomeAdjustCategoryEnum.WITHDRAWABLE.getId());
            adjustAccount.setAdjustType((Integer)detailsMap.get("income_type"));
            adjustAccount.setPrice((BigDecimal)detailsMap.get("price"));
            Integer operateType = (Integer) detailsMap.get("operate_type");
            operateType = IncomeAdjustOperateTypeEnum.ADD.getId() == operateType ? IncomeAdjustOperateTypeEnum.ADD.getId() : IncomeAdjustOperateTypeEnum.SUBSTRACT.getId();
            adjustAccount.setOperateType(operateType);
            adjustAccount.setAdjustUserId((Long)detailsMap.get("from_user_id"));
            adjustAccount.setReason((String)detailsMap.get("content"));
            adjustAccount.setRemark((String)detailsMap.get("content"));
            accountList.add(adjustAccount);
        }
        log.info("|手工调账|数据处理|耗时:{}", System.currentTimeMillis() - startTime);
        startTime = System.currentTimeMillis();

        // 处理数据
        List<AdjustAccount> successList = new ArrayList<>();
        List<AdjustAccount> errorList = new ArrayList<>();
        for (AdjustAccount account : accountList) {
            try {
                addOne(1, account);
                log.info("|手工调账|订单:{}|耗时:{}", account.getAdjustNo(), System.currentTimeMillis() - startTime);
                startTime = System.currentTimeMillis();
                successList.add(account);
            } catch (Exception e) {
                e.printStackTrace();
                log.info("|手工调账|处理数据|错误:{}", e.toString());
                account.setRemark(e.toString());
                errorList.add(account);
            }
        }
        Map<String, List<AdjustAccount>> retMap = new HashMap();
        retMap.put("SUCCESS", successList);
        retMap.put("ERROR", errorList);
        log.info("|手工调账|完成|SUCCESS:{}", successList);
        log.info("|手工调账|完成|ERROR:{}", errorList);
        log.info("|手工调账|结束|耗时:{}", System.currentTimeMillis() - flagTime);
        return retMap;
    }
}
