package com.zhuanbo.shop.api.scheduled;

import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.util.Sign;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;


@RunWith(SpringRunner.class)
@SpringBootTest
public class RefundOrderScheduledTest {

    @Autowired
    RefundOrderScheduled refundOrderScheduled;

    @Test
    public void refundOrder() {
        refundOrderScheduled.refundOrder();
    }

    @Test
    public void refundOrderQuery() {
    }

    @Test
    public void fsfls(){
        try {
            HashMap<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("mobile", "13717276548");
            paramsMap.put("orderNo", "2020040900005187");
            paramsMap.put("areaCode", "86");
            paramsMap.put("mercId", "888000000000004");
            paramsMap.put("platform", ConstantsEnum.PLATFORM_ZBMALL.stringValue());
            paramsMap.put("sysCnl", "WEB");
            paramsMap.put("timestamp", 1586421176L);

            String plain = Sign.getPlain(paramsMap) + "&key=B3lv0q99Xou8HCmSdeJrjxwI4WXaGGof";
            String signServer = Sign.sign(plain);
            System.out.println(plain);
            System.out.println(signServer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
