package com.zhuanbo.service.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhuanbo.core.entity.DepositOrder;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.entity.WithdrOrder;
import com.zhuanbo.service.vo.CashResultVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  专门作用于请求支付系统操作
 * </p>
 *
 * @author rome
 * @since 2019-01-22
 */
public interface ICashService {

    /**
     * 用户的余额
     * @param userId 用户id
     * @return
     */
	JSONObject balance(Long userId);

    /**
     * 用户各项余额
     * @param userId
     * @return
     */
    JSONObject balanceObj(Long userId);

    /**
     * 用户的余额 - 批量查询
     * @param userIds 用户id
     * @return
     */
    JSONArray balanceBatch(List<Long> userIds);

    /**
     * 查询提现订单状态
     * @param orderNo
     * @return
     */
    JSONObject queryWithdrOrder(WithdrOrder withdrOrder);

    /**
     * 充值
     * @param depositOrder
     * @return
     */
    CashResultVO charge(DepositOrder depositOrder);

    /**
     * 订单查询
     * @param orderNo
     * @return
     */
    CashResultVO queryOrder(String orderNo);

    /**
     * 统一下单
     * @param map
     * @return prePayNo 下单编码
     */
    String prePay(User user, Map<String, Object> map);

    /**
     * 请求
     * @param params
     * @return
     */
    JSONObject send (Map<String, Object> params);

    /**
     * 异常提现订单检查
     * @param withdrOrder
     * @return
     */
    JSONObject withdrApplyErrorCheck (WithdrOrder withdrOrder);
}
