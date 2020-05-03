package com.zhuanbo.service.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.OrderShip;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单物流信息表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-04-08
 */
public interface IOrderShipService extends IService<OrderShip> {

    /**
     * 根据第三方查询信息返回list结构的描述信息，只有time和desc，时间是倒序的
     * @param query
     * @return
     */
    List<JSONObject> toDetailList(String query);

    /**
     * 判断物流是否完成
     * @param detailList
     * @return
     */
    Boolean isSuccessbyDetail(List<JSONObject> detailList);

    /**
     * 把批量发货的消息格式转换
     * @param batchsMsg [orderNo|shipChannel|shipSn, orderNo|shipChannel|shipSn...]
     * @return {orderNo:[orderNo|shipChannel|shipSn, orderNo|shipChannel|shipSn...]}
     */
    Map<String, List<OrderShip>> toListByOrderNo(List<String> batchsMsg);
}
