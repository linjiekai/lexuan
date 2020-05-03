package com.zhuanbo.admin.api;

import com.zhuanbo.core.util.SpringContextUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.zhuanbo"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.zhuanbo.client.*")
@MapperScan("com.zhuanbo.service.mapper")
@EnableTransactionManagement
@ServletComponentScan(basePackages = {"com.zhuanbo.admin.api.handler.auth"})
public class AdminApplication extends SpringBootServletInitializer {

    @Bean
    public SpringContextUtil springContextUtil() {
        return new SpringContextUtil();
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(AdminApplication.class);
    }

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext run = SpringApplication.run(AdminApplication.class, args);
    }

}
