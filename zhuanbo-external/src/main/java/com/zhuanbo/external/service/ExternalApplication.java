package com.zhuanbo.external.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.zhuanbo")
public class ExternalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExternalApplication.class, args);
    }

}
