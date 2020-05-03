package com.zhuanbo.shop.api.scheduled;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderScheduledTest {

    @Autowired
    OrderScheduled orderScheduled;

    @Test
    public void query() {
    }

    @Test
    public void cancel() throws Exception{

        orderScheduled.cancel();
    }

    @Test
    public void success() {
    }
}