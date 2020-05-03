package com.zhuanbo.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.OrderShip;
import com.zhuanbo.service.mapper.OrderShipMapper;
import com.zhuanbo.service.service.IOrderShipService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单物流信息表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-04-08
 */
@Service
public class OrderShipServiceImpl extends ServiceImpl<OrderShipMapper, OrderShip> implements IOrderShipService {

    @Override
    public List<JSONObject> toDetailList(String query) {

        List<JSONObject> result = new ArrayList<>();

        JSONObject queryJson = JSON.parseObject(query);
        if ("200".equalsIgnoreCase(queryJson.getString("ret"))
                && queryJson.getJSONObject("data") != null
                && queryJson.getJSONObject("data").getJSONArray("traces") != null
                && queryJson.getJSONObject("data").getJSONArray("traces").size() > 0 ) {// 正常数据

            JSONArray tracesArray = queryJson.getJSONObject("data").getJSONArray("traces");

            List<JSONObject> jsonList = new ArrayList<>();
            for (int i = 0; i < tracesArray.size(); i++) {
                jsonList.add(tracesArray.getJSONObject(i));
            }
            jsonList.sort((m1, m2) -> {// 倒序
                if(m1.getString("time").equals(m2.getString("time"))){
                    return 0;
                }
                LocalDateTime t1 = LocalDateTime.parse(m1.getString("time"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                LocalDateTime t2 = LocalDateTime.parse(m2.getString("time"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                return t1.isBefore(t2) ? 1 : -1;
            });
            result.addAll(jsonList);
        }
        return result;
    }

    @Override
    public Boolean isSuccessbyDetail(List<JSONObject> detailList) {
        if (detailList == null) {
            return false;
        }
        if (detailList.size() > 0) {
            for (JSONObject jsonObject : detailList) {
                if (StringUtils.stripToEmpty(jsonObject.getString("desc")).contains("已签收")
                        || StringUtils.stripToEmpty(jsonObject.getString("desc")).contains("已完成")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Map<String, List<OrderShip>> toListByOrderNo(List<String> batchsMsg) {

        if (batchsMsg == null || batchsMsg.isEmpty()) {
            return new HashMap<>();
        }
        List<OrderShip> orderShipList = new ArrayList<>();
        OrderShip orderShip;
        String[] batchSplit;
        for (String batch : batchsMsg) {
            batchSplit = batch.split("\\|");
            orderShip = new OrderShip();
            orderShip.setOrderNo(batchSplit[0]);
            orderShip.setShipChannel(batchSplit[1]);
            orderShip.setShipSn(batchSplit[2]);
            orderShipList.add(orderShip);
        }
        Map<String, List<OrderShip>> collect = orderShipList.stream().collect(Collectors.groupingBy(OrderShip::getOrderNo));
        return collect;
    }
}
