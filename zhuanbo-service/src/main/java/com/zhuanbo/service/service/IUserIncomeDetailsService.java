package com.zhuanbo.service.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.UserIncomeDetails;
import com.zhuanbo.service.vo.MarginVO;
import com.zhuanbo.service.vo.UserIncomeDetailsStatVO;
import com.zhuanbo.service.vo.UserIncomeDetailsVO;

/**
 * <p>
 * 用户收益明细表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
public interface IUserIncomeDetailsService extends IService<UserIncomeDetails> {

	
	Page<UserIncomeDetailsVO> listMap(Page<UserIncomeDetailsVO> page, Map<String, Object> params);

    BigDecimal totalIncome(@Param("params") Map<String, Object> params);

    UserIncomeDetailsStatVO incomeStat(Map<String, Object> params);

    List<UserIncomeDetailsStatVO> incomeStatGruopBy(Map<String, Object> params);

    /**保证金变更记录列表
     * @Title: marginChangeList
     * @Description:
     * @param page
     * @param userId
     * @param userName
     * @param authNo
     * @param startDate
     * @param endDate
     * @return
     * @throws Exception
     */
    Page<MarginVO> marginChangeList(Page<MarginVO> page, Long userId, String userName, String authNo, String startDate,
                                    String endDate) throws Exception;
    
    /**
     * 收益转充值
     * @param params
     * @return
     */
    void incomde2Deposit(Order order);

    /**
     * 保证金总和
     * @param userId
     * @return
     */
    BigDecimal marginTotal(Long userId);
    
}
