package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.dto.AdminPointDTO;
import com.zhuanbo.core.dto.AdminUserIncomeDTO;
import com.zhuanbo.core.entity.DepositOrder;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.UserIncome;
import com.zhuanbo.core.entity.UserIncomeDetails;
import com.zhuanbo.core.entity.UserIncomeSyn;

import java.math.BigDecimal;
import java.util.List;


/**
 * <p>
 * 用户收益表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
public interface IUserIncomeService extends IService<UserIncome> {

	public void goodsOrder(Order order, UserIncomeSyn userIncomeSyn, Integer incomeType, Integer changeType, String content,Long adjustUserId) throws Exception;

	public void depositOrder(DepositOrder depositOrder, UserIncomeSyn userIncomeSyn, Integer incomeType, Integer changeType, String content,Long adjustUserId) throws Exception;
	
	/**
	 * 提现
	 *
	 * @param order
	 * @param userId
	 * @param operateIncome
	 * @param operateType
	 * @param changeType
	 * @param content
	 * @throws Exception
	 */
	public void withdrOrder(Order order, Long userId, BigDecimal operateIncome, Integer operateType, Integer changeType, String content) throws Exception;
	
	/**
	 * 退款
	 *
	 * @param userIncomeDetails
	 * @param content
	 * @throws Exception
	 */
	public void orderRefund(UserIncomeDetails userIncomeDetails, String content) throws Exception;


	/**
     * 生成一条收益表记录
     * @param userId 用户id
     * @return
     */
	public UserIncome makeUserIncome(Long userId);
	
	/**
	 * 在途收益转充值订单
	 * @param userId
	 * @throws Exception
	 */
    public void income2Deposit(Long userId, List<Integer> changeType) throws Exception;
    
    /**
     * 扣减在途收益
     * @param userId
     * @param uavaIncome
     * @throws Exception
     */
    public boolean subtractUavaIncome(Long userId, BigDecimal uavaIncome) throws Exception;

    /**
     * 添加在途收益
     * @param userId
     * @param price
     */
	public boolean addUavaIncome(Long userId, BigDecimal price);

	/**
	 * 根据用户id查询
	 * @param userId
	 * @return
	 */
	UserIncome getByUserId(Long userId);

	/**
	 * 积分充值
	 */
	void depositPoint(AdminPointDTO adminPointDTO);

	/**
	 * 添加总积分和可用积分
	 * @param userId
	 * @param point
	 */
	boolean addTotalAndUsablePoint(Long userId, Integer point);

	/**
	 * 使用积分(添加已使用,扣减可用)
	 * @param userId
	 * @param point
	 * @throws Exception
	 */
	boolean subtractUsablePoint(Long userId, Integer point);

	/**
	 * 获取积分信息
	 * @param adminPointDTO
	 * @return
	 */
	List<AdminUserIncomeDTO> pagePointInfo(IPage iPage, AdminPointDTO adminPointDTO);

	/**
	 * 扣减积分校验
	 *
	 * @param userId
	 * @param point
	 * @return
	 */
	boolean checkSubtracPoint(Long userId, Integer point);

}
