package com.zhuanbo.service.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.Cart;
import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.StatIncomeDay;
import com.zhuanbo.core.entity.StatUserSaleDay;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
public interface IOrderService extends IService<Order> {

    Page<Order> orderList(Page<Order> page, Map<String, Object> ew);

    /**
     * 批量推送
     * @param batchShip 格式：[orderNo|shipChannelshipSn,orderNo|shipChannelshipSn,orderNo|shipChannelshipSn...]
     * @param adminId
     */
    void batchShip(List<String> batchShip, Integer adminId);

    /**
     * 订单支付完成后一系列的处理，如推送，层级，分润等
     * @param orderNo
     */
	Map<String, Object> orderFinishPostProcess(String orderNo) throws Exception;

    /**
     * 自定义的查询(查询条件自定义)
     * @param page
     * @param ew
     * @return
     */
    IPage<Order> pageCustom(Page<Order> page, Map<String, Object> ew);
    
	/**
	 * 获取用户销售
	 * @param params
	 * @return
	 */
    StatIncomeDay statUserSale(Map<String, Object> params);

	/**
	 * 根据日期查询用户销售
	 * @param statDate
	 * @return
	 */
	List<StatUserSaleDay> listUserSale(String statDate);

	/**
	 * 返回用户的礼包信息
	 * @param uid 用户
	 * @param ignoreOrderNo 忽略的订单号
	 * @return [0,1,0,1],其中1：600礼包
	 */
	List<Integer> giftOrderNumber(Long uid, String ignoreOrderNo);

	/**
	 * 海外商品总价格是否在价格范围内
	 * @param checkedGoodsList 购物车数据
	 * @param limitPrice 限制价格
	 * @return true: 在范围内 false:不在范围内
	 */
	boolean isLimitPrice2Oversea(List<Cart> checkedGoodsList, BigDecimal limitPrice);

	/**
	 * 判断是否有海外商品
	 * @param checkedGoodsList 购物车数据
	 * @return true:有、false:无
	 */
	boolean haveOversea(List<Cart> checkedGoodsList);

	/**
	 * 生成发MQ的消息
	 * @param orderNo
	 * @param orderType
	 * @return
	 */
	Map<String, Object> mqMessageData(String orderNo, Integer orderType, List<Integer> typeSplit);

	/**
	 * 扣减库存
	 * @return
	 */
	JSONObject deductStock(String orderNo, String mobile, String userToken, String areaCode) throws Exception;

	/**
	 * 回退扣减库存
	 * @param no
	 * @param userToken
	 * @return
	 * @throws Exception
	 */
	JSONObject rollbackDeductStock(String no, String userToken) throws Exception;
}
