package com.zhuanbo.shop.api.controller;


import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.shop.api.thread.IndexAdsThread;
import com.zhuanbo.shop.api.thread.IndexQuickThread;
import com.zhuanbo.shop.api.thread.IndexShowCategoryThread;
import com.zhuanbo.shop.api.thread.IndexTopicThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * <p>
 * 首页
 * </p>
 *
 * @author rome
 * @since 2019-03-14
 */
@RestController
@RequestMapping("/shop/mobile/index")
@Slf4j
public class MobileIndexController {

    @Autowired
    @Qualifier("indexPoolExecutor")
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 列表(每个主题最多50个商品)
     *
     * @return
     */
    @PostMapping("/list")
    public Object list() throws Exception {
        Future<Object> ads = threadPoolTaskExecutor.submit(new IndexAdsThread());
        Future<Object> topic = threadPoolTaskExecutor.submit(new IndexTopicThread());
        Future<Object> quick = threadPoolTaskExecutor.submit(new IndexQuickThread());

        Future<Object> showCate = threadPoolTaskExecutor.submit(new IndexShowCategoryThread());

        Map<String, Object> data = new HashMap<>();
        data.put("ads", ads.get());
        data.put("topics", topic.get());
        data.put("quick", quick.get());
        data.put("showCategory", showCate.get());

        return ResponseUtil.ok(data);
    }
}
