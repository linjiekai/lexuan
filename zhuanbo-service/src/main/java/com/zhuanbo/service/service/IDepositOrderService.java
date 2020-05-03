package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.dto.AdminDepositOrderDTO;
import com.zhuanbo.core.dto.MobileDepositOrderDTO;
import com.zhuanbo.core.entity.AdjustAccount;
import com.zhuanbo.core.entity.DepositOrder;
import com.zhuanbo.core.entity.UserIncomeDetails;
import com.zhuanbo.service.vo.StatDepositOrderGroupByVo;
import com.zhuanbo.service.vo.StatDepositOrderVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 充值订单表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
public interface IDepositOrderService extends IService<DepositOrder> {
	/**
     * 收益明细转充值
     * @param userIncomeDetails
     * @param busiType 业务类型 01：充值;04：收益
     */
    DepositOrder saveFromIncomeDetails(UserIncomeDetails userIncomeDetails, String tradeCode, String busiType, String bankCode) throws Exception;

    /**
     * 充值完成后的操作
     * @param depositOrder
     */
    void finishDeposit(DepositOrder depositOrder) throws Exception;

    /**
     * 查询列表
     */
    Object list(AdminDepositOrderDTO dto) throws Exception;

    Object exList(AdminDepositOrderDTO dto);

    /**
     * 站外充值记录(shop)
     *
     * @param depositOrderDTO
     * @return
     */
    Map<String, Object> exList(MobileDepositOrderDTO depositOrderDTO);

    /**
     * 充值订单统计
     * @param params
     * @return
     */
	StatDepositOrderVo statDepositOrder(Map<String, Object> params) throws Exception;

    /**
     * 充值订单分类统计统计
     * @param params
     * @return
     */
	List<StatDepositOrderGroupByVo> statDepositOrderGroupBy(Map<String, Object> params) throws Exception;

    /**
     * @Description(描述): 导出报表
     * @auther: Jack Lin
     * @param :[dto, response, request]
     * @return :void
     * @date: 2019/8/29 16:26
     */
    void exportExcel(AdminDepositOrderDTO dto, HttpServletResponse response, HttpServletRequest request) throws Exception;

    /**
     * 调账处理
     * @param adjustAccount
     */
    String finishDepositToAdjust(AdjustAccount adjustAccount) throws Exception;

    /**
     * 校验保证金余额
     *
     * @param depositOrder
     */
    boolean checkMarginBalance(DepositOrder depositOrder);

    /**
     * 查找一个
     * @param depositOrder
     * @return
     */
    DepositOrder existOne(DepositOrder depositOrder);

    /**
     * 同步数据到php
     */
    void syncDepositOrderToProfit(Map<String, Object> profitOrderMap);

}
