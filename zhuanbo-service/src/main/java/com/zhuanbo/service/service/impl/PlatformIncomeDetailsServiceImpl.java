package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.constants.OrderStatus;
import com.zhuanbo.core.constants.PlatformIncomeDetailsStatusEnum;
import com.zhuanbo.core.constants.PlatformIncomeOperateTypeEnum;
import com.zhuanbo.core.constants.PlatformIncomeOrderTypeEnum;
import com.zhuanbo.core.dto.AdminPlatformIncomeDetailsDTO;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.PlatformIncomeDetails;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.service.mapper.PlatformIncomeDetailsMapper;
import com.zhuanbo.service.service.IPlatformIncomeDetailsService;
import com.zhuanbo.service.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * <p>
 * 平台收益明细表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-10-29
 */
@Service
@Slf4j
public class PlatformIncomeDetailsServiceImpl extends ServiceImpl<PlatformIncomeDetailsMapper, PlatformIncomeDetails> implements IPlatformIncomeDetailsService {

    @Autowired
    private IUserService userService;

    /**
     * 平台收益明细列表信息
     *
     * @param incomeDetailsDTO
     * @return
     */
    @Override
    public Map<String, Object> list(AdminPlatformIncomeDetailsDTO incomeDetailsDTO) {
        Integer page = incomeDetailsDTO.getPage();
        Integer limit = incomeDetailsDTO.getLimit();
        Long userId = incomeDetailsDTO.getUserId();
        String sourceOrderNo = incomeDetailsDTO.getSourceOrderNo();
        String orderNo = incomeDetailsDTO.getOrderNo();
        Integer incomeType = incomeDetailsDTO.getIncomeType();
        Integer orderType = incomeDetailsDTO.getOrderType();
        Integer operateType = incomeDetailsDTO.getOperateType();
        QueryWrapper<PlatformIncomeDetails> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", PlatformIncomeDetailsStatusEnum.EFFECTIVE.getId());
        queryWrapper.orderByDesc("add_time");
        if (null != userId) {
            queryWrapper.eq("user_id", userId);
        }
        if (StringUtils.isNotBlank(sourceOrderNo)) {
            queryWrapper.eq("source_order_no", sourceOrderNo);
        }
        if (StringUtils.isNotBlank(orderNo)) {
            queryWrapper.eq("order_no", orderNo);
        }
        if (null != incomeType) {
            queryWrapper.eq("income_type", incomeType);
        }
        if (null != orderType) {
            queryWrapper.eq("order_type", orderType);
        }
        if (null != operateType) {
            queryWrapper.eq("operate_type", operateType);
        }
        IPage<PlatformIncomeDetails> iPage = new Page(page, limit);
        iPage = this.page(iPage, queryWrapper);
        List<PlatformIncomeDetails> incomeDetailsList = iPage.getRecords();
        List<Long> userIds = incomeDetailsList.stream().map(PlatformIncomeDetails::getUserId).collect(toList());
        List<User> userList = userService.list(new QueryWrapper<User>().eq("deleted", 0).in("id", userIds));
        List<AdminPlatformIncomeDetailsDTO> detailsDTOS = new ArrayList<>();
        if (incomeDetailsList != null && incomeDetailsList.size() > 0) {
            incomeDetailsList.forEach(incomeDetails -> {
                AdminPlatformIncomeDetailsDTO detailsDTO = new AdminPlatformIncomeDetailsDTO();
                BeanUtils.copyProperties(incomeDetails, detailsDTO);
                detailsDTO.setName("");
                detailsDTO.setNickname("");
                Long userIdTemp = detailsDTO.getUserId();
                if (userIdTemp != null) {
                    if (userList != null && userList.size() > 0) {
                        userList.forEach(userTemp -> {
                            if (userIdTemp.equals(userTemp.getId())) {
                                detailsDTO.setName(userTemp.getName());
                                detailsDTO.setNickname(userTemp.getNickname());
                            }
                        });
                    }
                }
                detailsDTOS.add(detailsDTO);
            });
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("total", iPage.getTotal());
        dataMap.put("items", detailsDTOS);
        return dataMap;
    }

    /**
     * 保存平台收益明细
     *
     * @param userId        用户id
     * @param orderNo       订单号
     * @param orderType     订单类型 0：交易 1：退款
     * @param sourceOrderNo 原订单号
     * @param price         订单金额
     * @param operateType   操作类型 1：增加收益 2：减少收益
     * @param incomeType    收益类型
     * @param status        收益状态
     * @param content       内容
     */
    @Override
    public PlatformIncomeDetails save(Long userId, String orderNo, Integer orderType, String sourceOrderNo, BigDecimal price, Integer operateType, Integer incomeType, Integer status, String content) {
        PlatformIncomeDetails platformIncomeDetails = new PlatformIncomeDetails();
        platformIncomeDetails.setUserId(userId);
        platformIncomeDetails.setOrderNo(orderNo);
        platformIncomeDetails.setOrderType(orderType);
        platformIncomeDetails.setSourceOrderNo(sourceOrderNo);
        platformIncomeDetails.setPrice(price);
        platformIncomeDetails.setOperateType(operateType);
        platformIncomeDetails.setIncomeType(incomeType);
        platformIncomeDetails.setStatus(status);
        platformIncomeDetails.setIncomeDate(DateUtil.date10());
        platformIncomeDetails.setIncomeTime(DateUtil.time8());
        platformIncomeDetails.setContent(content);
        platformIncomeDetails.setUpdateTime(LocalDateTime.now());
        this.save(platformIncomeDetails);
        return platformIncomeDetails;
    }

    /**
     * 保存平台收益明细 - 退款
     *
     * @param order 退款原订单
     */
    @Override
    public void orderRefund(Order order, String orderRefundNo) {
        log.info("|平台收益退款|开始，orderNo:{}",order.getOrderNo());
        // 退款:平台收益明细记录
        Long userId = order.getUserId();
        String orderNo = order.getOrderNo();
        BigDecimal price = order.getPrice();
        PlatformIncomeDetails platformIncomeDetails = getOne(new QueryWrapper<PlatformIncomeDetails>().eq("order_no", orderNo).eq("order_type", PlatformIncomeOrderTypeEnum.TRADE.getId()));
        if (platformIncomeDetails == null ) {
            log.info("|平台收益退款|无平台收益信息，orderNo:{}", order.getOrderNo());
            return;
        }
        String orderStatus = order.getOrderStatus();
        Integer status = platformIncomeDetails.getStatus();
        // 判断原订单状态是否成功,成功:新增平台收益明细,未成功:修改原收益状态为:3(已过期)
        if (!OrderStatus.SUCCESS.getId().equals(orderStatus)) {
            if (status != PlatformIncomeDetailsStatusEnum.EXPIRED.getId()) {
                platformIncomeDetails.setStatus(PlatformIncomeDetailsStatusEnum.EXPIRED.getId());
                updateById(platformIncomeDetails);
            }
            log.info("|平台收益退款|原订单状态未成功：{}，orderNo:{}",orderStatus,order.getOrderNo());
            return;
        }
        save(userId, orderNo, PlatformIncomeOrderTypeEnum.REFUND.getId(),
                orderRefundNo, price, PlatformIncomeOperateTypeEnum.SUBSTRACT.getId(),
                platformIncomeDetails.getIncomeType(), PlatformIncomeDetailsStatusEnum.EFFECTIVE.getId(), platformIncomeDetails.getContent() + PlatformIncomeOrderTypeEnum.REFUND.getName());
        log.info("|平台收益退款|完成，orderNo:{}",order.getOrderNo());
    }

    /**
     * 日提现额度计算
     *
     * @param localDate
     * @return
     */
    @Override
    public BigDecimal sumPriceByIncomeTypeAndAddTime(Integer incomeType, LocalDate localDate) {
        QueryWrapper<PlatformIncomeDetails> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("SUM(price) AS price")
                .eq("income_type", incomeType)
                .eq("status", PlatformIncomeDetailsStatusEnum.EFFECTIVE.getId())
                .eq("DATE(add_time)", localDate);
        PlatformIncomeDetails platformIncomeDetails = this.getOne(queryWrapper);
        if (platformIncomeDetails == null || platformIncomeDetails.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        return platformIncomeDetails.getPrice();
    }


}
