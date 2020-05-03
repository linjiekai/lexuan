package com.zhuanbo.shop.api;

import com.zhuanbo.core.util.SpringContextUtil;
import feign.Retryer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.zhuanbo"})
@EnableTransactionManagement
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.zhuanbo.client.*")
@MapperScan("com.zhuanbo.service.mapper")
@ServletComponentScan(basePackages = {"com.zhuanbo.shop.api.handler.auth"})
@EnableScheduling
public class ShopApiApplication extends SpringBootServletInitializer {

    @Bean
    public SpringContextUtil springContextUtil() {
        return new SpringContextUtil();
    }

    @Bean
    Retryer feignRetryer() {
        return Retryer.NEVER_RETRY;
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ShopApiApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ShopApiApplication.class, args);
    }
}

