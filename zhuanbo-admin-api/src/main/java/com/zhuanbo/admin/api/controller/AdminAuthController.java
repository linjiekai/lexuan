package com.zhuanbo.admin.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.ImmutableMap;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.util.CharUtil;
import com.zhuanbo.core.util.JacksonUtil;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.util.bcrypt.BCryptPasswordEncoder;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.Admin;
import com.zhuanbo.service.service.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/login")
@Validated
public class AdminAuthController {
    private final Log logger = LogFactory.getLog(AdminAuthController.class);

    @Autowired
    private IAdminService adminService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private IProductService productService;
    @Autowired
    private IOrderService orderService;

    /*
     *  { username : value, password : value }
     */
    @PostMapping("/login")
    public Object login(@RequestBody String body){
        String username = JacksonUtil.parseString(body, "username");
        String password = JacksonUtil.parseString(body, "password");

        if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
            return ResponseUtil.badArgument();
        }
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", ConstantsEnum.DELETED_0.integerValue());
        if(StringUtils.isNotBlank(username)){
            queryWrapper.eq("username", username);
        }
        List<Admin> adminList = adminService.list(queryWrapper);
        Assert.state(adminList.size() < 2, "同一个用户名存在两个账户");
        if(adminList.size() == 0){
            return ResponseUtil.badArgumentValue();
        }
        Admin admin = adminList.get(0);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if(!encoder.matches(password, admin.getPassword())){
            return ResponseUtil.fail(403, "账号密码不对");
        }

        // token
        String token = CharUtil.getRandomString(32);
        RedisUtil.set(token, admin.getId());
        return ResponseUtil.ok(token);
    }

    /*
     *
     */
    @PostMapping("/logout")
    public Object login(@LoginAdmin Integer adminId){
        if(adminId == null){
            return ResponseUtil.unlogin();
        }

        return ResponseUtil.ok();
    }

    @GetMapping("/dashboard")
    public Object dashboard () {

        return ResponseUtil.ok(ImmutableMap.of(
                "userTotal", userService.count(null),
                "goodsTotal", goodsService.count(null),
                "productTotal", productService.count(null),
                "orderTotal", orderService.count(null)));
    }
}
