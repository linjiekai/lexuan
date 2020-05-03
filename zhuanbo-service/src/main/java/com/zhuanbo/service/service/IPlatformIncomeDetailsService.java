package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.dto.AdminPlatformIncomeDetailsDTO;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.PlatformIncomeDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * <p>
 * 平台收益明细表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-10-29
 */
public interface IPlatformIncomeDetailsService extends IService<PlatformIncomeDetails> {

    /**
     * 平台收益明细列表信息
     *
     * @return
     */
    Map<String, Object> list(AdminPlatformIncomeDetailsDTO incomeDetailsDTO);

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
    PlatformIncomeDetails save(Long userId, String orderNo, Integer orderType, String sourceOrderNo, BigDecimal price, Integer operateType, Integer incomeType, Integer status, String content);

    /**
     * 保存平台收益明细 - 退款
     *
     * @param order 退款原订单
     * @param orderRefundNo
     */
    void orderRefund(Order order, String orderRefundNo);

    /**
     * 日提现额度计算
     *
     * @param localDate
     * @return
     */
    BigDecimal sumPriceByIncomeTypeAndAddTime(Integer incomeType, LocalDate localDate) ;

}
