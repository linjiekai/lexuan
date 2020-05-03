package com.zhuanbo.shop.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadPoolConfig {

    @Value("${indexPool.corePoolSize}")
    private  String corePoolSize;
    @Value("${indexPool.maxPoolSize}")
    private  String maxPoolSize;
    @Value("${indexPool.keepAliveSeconds}")
    private  String keepAliveSeconds;
    @Value("${indexPool.queueCapacity}")
    private  String queueCapacity;

    //首页线程池
    @Bean("indexPoolExecutor")
    public ThreadPoolTaskExecutor applicationThreadPoolConfig(){
        ThreadPoolTaskExecutor threadPoolExecutor = new ThreadPoolTaskExecutor();
        threadPoolExecutor.setCorePoolSize(Integer.valueOf(corePoolSize));
        threadPoolExecutor.setMaxPoolSize(Integer.valueOf(maxPoolSize));
        threadPoolExecutor.setKeepAliveSeconds(Integer.valueOf(keepAliveSeconds));
        threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolExecutor.setQueueCapacity(Integer.valueOf(queueCapacity));
        threadPoolExecutor.setThreadNamePrefix("threadPoolExecutor-");
        return threadPoolExecutor;
    }
}
