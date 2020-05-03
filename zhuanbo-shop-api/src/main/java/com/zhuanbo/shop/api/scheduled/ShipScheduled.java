package com.zhuanbo.shop.api.scheduled;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.entity.Ship;
import com.zhuanbo.core.util.HttpUtil;
import com.zhuanbo.core.util.LogUtil;
import com.zhuanbo.service.service.IShipService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ShipScheduled {

    final static String OK_CODE = "200";

    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private IShipService iShipService;

	// 用来标识定时器状态 1：开启 0：关闭
 	public static Integer SCHEDULED_STATUS = 0;

    /**
     * 同步快递公司,每个月1号24点执行一次
     */
    @Scheduled(cron = "${scheduled.ship-shipCompay}")
    public void shipCompay(){
    	if (Constants.SCHEDULER_SWITCH == 0) {
			  SCHEDULED_STATUS = 0;
			  return;
		  }

		SCHEDULED_STATUS = 1;

		try {
	        Map<String, String> headers = new HashMap<>();
	        headers.put("Authorization", "APPCODE " + authConfig.getShipAppCode());
	        String s = HttpUtil.sendGet(authConfig.getShipCompanyUrl(), "", headers);

	        JSONObject resultJSON = JSON.parseObject(s);
	        if (OK_CODE.equals(resultJSON.getString("ret"))) {
	            JSONArray array = resultJSON.getJSONArray("data");
	            JSONObject data;
	            Ship ship;
	            List<Ship> shipList = new ArrayList<>();

	            for (int i = 0 ;i < array.size(); i++) {
	                data = array.getJSONObject(i);
	                ship = new Ship();
	                ship.setName(data.getString("com"));
	                ship.setLogoUrl(data.getString("src"));
	                ship.setTel(data.getString("tel"));
	                ship.setShipChannel(data.getString("comid"));
	                ship.setSite(data.getString("site"));
	                shipList.add(ship);
	            }
	            List<Ship> oldShipList = iShipService.list(null);

	            if (!oldShipList.isEmpty()) {
	                List<String> collect = oldShipList.stream().map(x -> x.getShipChannel()).collect(Collectors.toList());
	                LocalDateTime now = LocalDateTime.now();
	                for(Ship ss : shipList) {
	                	if (Constants.SCHEDULER_SWITCH == 0) {
							        SCHEDULED_STATUS = 0;
							        LogUtil.SCHEDULED.info("同步快递公司信息定时器已关闭SCHEDULED_STATUS[{}]........", SCHEDULED_STATUS);
							        return;
	                	}
	                    if (collect.contains(ss.getShipChannel())) {
	                        ss.setUpdateTime(now);
	                        iShipService.update(ss, new UpdateWrapper<Ship>().eq("ship_channel", ss.getShipChannel()));
	                    } else {
	                        ss.setAddTime(now);
	                        ss.setUpdateTime(now);
	                        iShipService.save(ss);
	                    }
	                }
	            }
	        } else {
	            log.error("获取快递公司失败:{}", resultJSON);
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}

		SCHEDULED_STATUS = 0;
    }

    /**
     * 订单物流跟踪， 每6个小时同步一次
     */
    /*@Scheduled(cron = "${scheduled.ship-orderShip}")
    public void orderShip(){
        LogUtil.SCHEDULED.info("定时器：订单物流跟踪,{}", LocalDateTime.now());
        // 待收货
        List<Order> orderList = iOrderService.list(new QueryWrapper<Order>().eq("order_status", OrderStatus.WAIT_DELIVER.getId()));

        if (!orderList.isEmpty()) {
            OrderDescribe orderDescribe;
            OrderShip orderShip;
            JSONObject traceJSON;

            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "APPCODE " + authConfig.getShipAppCode());
            String s;
            JSONObject resultJSON;
            JSONObject dataJSON;
            JSONArray tracesJSON;

            int queryNumber = 0;
            for (Order order : orderList) {

                try {
                    orderDescribe = iShopOrderDescribeService.getOne(new QueryWrapper<OrderDescribe>().eq("order_no", order.getOrderNo()));
                    if (orderDescribe == null) {
                        continue;
                    }
                    queryNumber++;
                    s = HttpUtil.sendGet(authConfig.getShipTraceUrl(), "comid=" + orderDescribe.getShipChannel() + "&number=" + orderDescribe.getShipSn(), headers);
                    if (StringUtils.isBlank(s)) {
                        continue;
                    }
                    resultJSON = JSON.parseObject(s);
                    if (resultJSON == null || !OK_CODE.equals(resultJSON.getString("ret"))) {
                        continue;
                    }
                    orderShip = iOrderShipService.getOne(new QueryWrapper<OrderShip>().eq("order_no", order.getOrderNo()));
                    if (orderShip == null) {
                        continue;
                    }
                    dataJSON = resultJSON.getJSONObject("data");
                    if(dataJSON == null){
                        continue;
                    }
                    tracesJSON = dataJSON.getJSONArray("traces");// 跟踪信息
                    if (tracesJSON == null || tracesJSON.size() == 0) {
                        continue;
                    }
                    orderShip.setRouteInfo(tracesJSON.toJSONString());
                    // 已完成:S 已取消:C 待收货:WD
                    int sNumber = 0;
                    for (int i = 0; i < tracesJSON.size(); i++) {
                        traceJSON = tracesJSON.getJSONObject(i);
                        if (StringUtils.stripToEmpty(traceJSON.getString("desc")).contains("已签收")
                                || StringUtils.stripToEmpty(traceJSON.getString("desc")).contains("已完成")) {
                            sNumber++;
                        }
                    }
                    orderShip.setOrderStatus(sNumber > 0 ? OrderStatus.SUCCESS.getId() : orderShip.getOrderStatus());
                    order.setOrderStatus(sNumber > 0 ? OrderStatus.SUCCESS.getId() : orderShip.getOrderStatus());
                    iOrderShipService.updateById(orderShip);
                    iOrderService.updateById(order);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("定时，物流信息，异常：{}", e);
                }
            }
            LogUtil.SCHEDULED.info("查询物流的订单数量,{}", queryNumber);
        }
    }*/
}
