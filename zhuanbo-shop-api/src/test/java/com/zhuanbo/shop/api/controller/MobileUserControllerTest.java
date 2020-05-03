package com.zhuanbo.shop.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class MobileUserControllerTest {

    @Autowired
    private  MobileUserController mobileUserController;

    @Test
    public void queryUserInfoByInterior() throws Exception{
    }
}