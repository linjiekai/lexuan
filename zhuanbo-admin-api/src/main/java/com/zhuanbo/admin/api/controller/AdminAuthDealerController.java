package com.zhuanbo.admin.api.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.ImmutableMap;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.annotation.LoginDealersAdmin;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.PtLevelType;
import com.zhuanbo.core.entity.Admin;
import com.zhuanbo.core.entity.AdminDealer;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.util.CharUtil;
import com.zhuanbo.core.util.JacksonUtil;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.util.bcrypt.BCryptPasswordEncoder;
import com.zhuanbo.service.service.IAdminDealerService;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.IGoodsService;
import com.zhuanbo.service.service.IOrderService;
import com.zhuanbo.service.service.IProductService;
import com.zhuanbo.service.service.IUserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/dl/login")
@Validated
public class AdminAuthDealerController {
    private final Log logger = LogFactory.getLog(AdminAuthDealerController.class);

    @Autowired
    private IAdminDealerService adminDealerService;
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
    public Object login(@RequestBody String body) throws Exception {
        String username = JacksonUtil.parseString(body, "username");
        String password = JacksonUtil.parseString(body, "password");

        if(StringUtils.isBlank(username) || StringUtils.isBlank(password)){
            return ResponseUtil.badArgument();
        }

        Map<String,Object> data = new HashMap<>();

        //admin登录
        QueryWrapper<AdminDealer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", ConstantsEnum.DELETED_0.integerValue());
        queryWrapper.eq("username", username);
        List<AdminDealer> adminList = adminDealerService.list(queryWrapper);
        if(CollectionUtil.isNotEmpty(adminList)){
            Assert.state(adminList.size() < 2, "同一个用户名存在两个账户");
            if(adminList.size() == 0){
                return ResponseUtil.badArgumentValue();
            }
            AdminDealer admin = adminList.get(0);

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if(!encoder.matches(password, admin.getPassword())){
                return ResponseUtil.fail(403, "账号密码不对");
            }

            // token
            String token = CharUtil.getRandomString(32);
            RedisUtil.set(token, admin.getId());
            return ResponseUtil.ok(token);
        }

        //联创登录
        QueryWrapper<User> qw = new QueryWrapper<User>();
        qw.eq("deleted", ConstantsEnum.DELETED_0.integerValue());
        qw.eq("user_name", username);
        qw.eq("pt_level", PtLevelType.BASE.getId());
        User dbUser = userService.getOne(qw);
        if(null == dbUser){
            return ResponseUtil.fail(999, "用户不存在");
        }
        if(!ConstantsEnum.USER_STATUS_1.integerValue().equals(dbUser.getStatus())){
            return ResponseUtil.fail(999,ConstantsEnum.USER_STATUS_0.integerValue().equals(dbUser.getStatus()) ? "当前账号属于待审核状态、请等审核后再登录" : "当前账号属于[冻结/黑名单]、请等联系管理员");
        }

        String hexPassword = DigestUtils.sha1Hex(password.getBytes("UTF-16LE"));
        if (!hexPassword.equals(dbUser.getPassword())) {
            return ResponseUtil.fail(999, "账号密码不对");
        }

        // token
        String token = CharUtil.getRandomString(32);
        RedisUtil.set(token, dbUser.getId());
        return ResponseUtil.ok(token);

    }

    /*
     *
     */
    @PostMapping("/logout")
    public Object logout(@LoginDealersAdmin Integer adminId){
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
