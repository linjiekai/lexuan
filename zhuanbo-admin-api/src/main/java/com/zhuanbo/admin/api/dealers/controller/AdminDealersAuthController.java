package com.zhuanbo.admin.api.dealers.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.ImmutableMap;
import com.zhuanbo.core.annotation.LoginDealersAdmin;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.PtLevelType;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.util.CharUtil;
import com.zhuanbo.core.util.JacksonUtil;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.core.util.ResponseUtil;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/admin/dealers")
@Validated
public class AdminDealersAuthController {
    private final Log logger = LogFactory.getLog(AdminDealersAuthController.class);

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
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", ConstantsEnum.DELETED_0.integerValue());
        queryWrapper.eq("username", username);
        
        User user = userService.getOne(queryWrapper);
        if(user == null){
            return ResponseUtil.badArgumentValue();
        }
        
        if (user.getPtLevel() != PtLevelType.PARTNER.getId()) {
        	return ResponseUtil.fail(403, "用户等级不正确");
        }
        
        String hexPassword;
        try {
            hexPassword = DigestUtils.sha1Hex(user.getPassword().getBytes("UTF-16LE"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        if (!hexPassword.equals(user.getPassword())) {
        	return ResponseUtil.fail(403, "账号密码不对");
        }
        

        // token
        String token = CharUtil.getRandomString(32);
        RedisUtil.set(token, user.getId());
        return ResponseUtil.ok(token);
    }
    
    /*
     *
     */
    @PostMapping("/logout")
    public Object login(@LoginDealersAdmin Integer adminId){
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
