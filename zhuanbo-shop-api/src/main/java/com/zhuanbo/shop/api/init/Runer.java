package com.zhuanbo.shop.api.init;

import com.zhuanbo.service.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Runer implements CommandLineRunner {

    @Autowired
    private IUserService iUserService;

    @Override
    public void run(String... args) throws Exception {
        // 邀请码
        new Thread(new InviteRuner(iUserService)).start();
    }


    public static class InviteRuner implements Runnable {

        private IUserService iUserService;

        public InviteRuner(IUserService iUserService) {
            this.iUserService = iUserService;
        }
        @Override
        public void run() {
            iUserService.checkInviteCodeNumber();
        }
    }
}
