package com.zhuanbo.shop.api.pay.test;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.zhuanbo.client.server.client.PayClient;
import com.zhuanbo.client.server.dto.ResponseDTO;
import com.zhuanbo.core.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class PayTest {
    @Autowired
    protected ApplicationContext ctx;
    @Autowired
    private PayClient payClient;
   /* @Autowired
    private ZhuanboLiveClient zhuanboLiveClient;*/


    private String prePayNo;
    Map<String, Object> msg = CollectionUtil.newHashMap();

    @Before
    public void setUp() throws Exception {
        //super.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        // 请求公共参数
        msg.put("userId", 942l); //用户id
        msg.put("sysCnl", "IOS");
        msg.put("mercId", "888000000000004");
        msg.put("clientIp", "127.0.0.1");
        msg.put("timestamp", (int) (System.currentTimeMillis() / 1000));
        msg.put("requestId", System.currentTimeMillis());
        msg.put("platform", "ZBMALL");

    }

    //预支付订单
    @Test
    public void DirectPrePay() throws Exception {
        msg.put("methodType", "DirectPrePay");
        msg.put("orderNo", System.currentTimeMillis() + "");
        msg.put("orderDate", DateUtil.date10());
        msg.put("orderTime", DateUtil.date8());
        msg.put("price", 1);
        msg.put("periodUnit", "00");
        msg.put("period", "30");
        msg.put("mobile", "13825051122");
        msg.put("busiType", "05");
        msg.put("tradeCode", "02");
        msg.put("clientIp", "192.168.0.1");
        msg.put("goodsId", 111);
        msg.put("goodsName", "商品名称");
        msg.put("notifyUrl", "http://127.0.0.1:15111/shop/mobile/pay/notify");
        msg.put("X-MPMALL-SignVer", "v1");
        msg.put("X-MPMALL-Sign", "111111");
        MDC.put("X-MPMALL-Sign-PAY", String.valueOf(UUID.randomUUID()));

        ResponseDTO unified = payClient.unified(msg);
        System.out.println(JSONUtil.toJsonStr(unified));
    }



}