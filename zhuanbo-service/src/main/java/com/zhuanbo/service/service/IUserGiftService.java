package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.Cart;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.UserGift;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户礼包表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-11-22
 */
public interface IUserGiftService extends IService<UserGift> {

    /**
     * 可提货数量
     * @param uid 用户id
     * @return
     */
    Integer pickNumber(Long uid);

    /**
     * h5
     * @param uid
     * @return
     */
    Map status(Long uid);

    /**
     * 简单生成一条记录
     * @param uid
     */
    void simpleMakeOne(Long uid);

    /**
     * 根据用户id获取记录
     * @param uid
     * @return
     */
    UserGift getByUserId(Long uid);

    /**
     * 更新基本礼包和赠送礼包数量
     * @param uid 用户id
     * @param baseGiftNumber 基础礼包
     * @param sendGiftNumber 赠送礼包
     * @return
     */
    boolean updateGiftNumber(Long uid, Integer baseGiftNumber, Integer sendGiftNumber);

    /**
     * 购买礼包扣减上级的礼包
     * @param user
     * @param purceType
     * @param inviteUserId
     * @param cartList
     * @param order
     */
    void doCutNumber(User user, Integer purceType, Long inviteUserId, List<Cart> cartList, Order order);

    /**
     * 扣减礼包并返回分润上级
     * @param user
     * @return
     */
    Long cutNbackProfitID(User user, String orderNo, Integer purceType, Integer normalNumber, User inviteUser, Integer giftNumber, BigDecimal price);

    Long cutNbackProfitID2(User user, String orderNo, Integer purceType, Integer normalNumber, User inviteUser, Integer giftNumber, BigDecimal price);

    /**
     * 获取礼包数量
     * @param cartList
     * @return
     */
    int giftNumber(List<Cart> cartList);

    /**
     * 扣减赠送礼包数量(赠送礼包使用数量增加)
     * @param uid 用户id
     * @param sendNumber 扣减数量
     */
    boolean doCutSendGiftNumber(Long uid, Integer sendNumber);

    /**
     * 扣减赠送礼包锁库存
     * @param uid
     * @param sendNumber
     * @return
     */
    boolean doCutSendLockGiftNumber(Long uid, Integer sendNumber);

    /**
     * 扣减基础礼包数量（基础礼包使用数量增加）
     * @param uid
     * @param baseNumber
     * @return
     */
    boolean doCutBaseGiftNumber(Long uid, Integer baseNumber);

    /**
     * 用户提取礼包
     * @param user 用户
     * @param number 提取数量
     */
    void pick(User user, Integer number, Order order);

    /**
     * 订单取消归还礼包数量
     * @param order
     */
    void returnGiftNumber(Order order, String orderOldStatus);

	/**
     * 添加赠送礼包
     * @param uid 用户id
     * @param number 数量
     * @return
     */
    boolean doAddSendGiftNumber(Long uid, Integer number);
    
    /**
     * 累加已使用的赠送礼包
     * @param uid 用户id
     * @param number 数量
     * @return
     */
    boolean doAddSendGiftNumberUse(Long uid, Integer number);

    /**
     * 赠礼锁库存减少(取消订单时)
     * @param uid
     * @param number
     * @return
     */
    boolean doBackSendGiftNumber(Long uid, Integer number);

    /**
     * 添加基础礼包
     * @param uid 用户id
     * @param number 数量
     * @return
     */
    boolean doAddBaseGiftNumber(Long uid, Integer number);
    
    /**
     * VIP和县级订单完成(30件以上)添加或更新礼包
     * @param order
     */
	void saveOrUpdateGiftByOrder(Order order, User user, Integer buyNum);
	
	/**
     * 添加或更新赠送礼包
     * @param uesrId
     */
	void saveOrUpdateGift(Long uesrId);
	
	/**
	 * 累加邀请VIP用户数量
	 * @param order
	 * @param user
	 * @param buyNum
	 */
	void addVipNum(Order order, User user, Integer buyNum);

    /**
     * 扣减基础礼包线下数量
     * @param userId
     * @param number
     */
	void doCutBaseNumOffline(Long userId, Integer number);

    /**
     * 添加基础礼包线下数量
     * @param userId
     * @param number
     */
    void doAddBaseNumOffline(Long userId, Integer number);

    /**
     * 剩下可锁定的库存
     * @param uid
     * @return
     */
    int leaveGiftNum(Long uid);

    /**
     * 订单取消的相关操作
     * @param orderNo
     */
    void cancel(String orderNo);

    /**
     * 礼包数量
     * @param uid
     * @return {onLineCount:线上， onLineCount:线下， allCount:全部}
     */
    Map<String, Object> count(Long uid);
}
