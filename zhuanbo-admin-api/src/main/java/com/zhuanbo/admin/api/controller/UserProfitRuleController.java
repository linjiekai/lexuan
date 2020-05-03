package com.zhuanbo.admin.api.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.dto.AdminUserProfitRuleDTO;
import com.zhuanbo.core.entity.UserProfitRule;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.IUserProfitRuleService;
import com.zhuanbo.service.utils.LogOperateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户利润分配规则表 （基础课时费规则）
 */
@RestController
@RequestMapping("/admin/user/profit/rule")
public class UserProfitRuleController {

    final String TYPE = "type";

    @Autowired
    private IUserProfitRuleService iUserProfitRuleService;
    @Autowired
    private IAdminService iAdminService;
    @Autowired
    private RedissonDistributedLocker redissonLocker;
    private final static String LOCK_USERPROFITRULE_UPDATE = "lock_userprofitrule_update_";
    /**
     * 列表
     * @return
     */
    @PostMapping("/list")
    public Object list(@LoginAdmin Integer adminId,@RequestBody AdminUserProfitRuleDTO dto) {
        IPage<UserProfitRule> iPage = new Page<>(dto.getPage(), dto.getLimit());
        QueryWrapper<UserProfitRule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("profit_type", dto.getProfitType().intValue());
        IPage<UserProfitRule> userProfitRuleIPage = iUserProfitRuleService.page(iPage, queryWrapper);

        Map<String, Object> backMap = new HashMap<>();
        backMap.put("total", userProfitRuleIPage.getTotal());
        backMap.put("items", userProfitRuleIPage.getRecords());
        return ResponseUtil.ok(backMap);
    }

    @PostMapping("/update")
    public Object update(@LoginAdmin Integer adminId, @RequestBody AdminUserProfitRuleDTO dto) {
        LogOperateUtil.log("基础课时费管理", "修改", String.valueOf(dto.getId()), adminId.longValue(), 0);

        UserProfitRule userProfitRule = new UserProfitRule();
        BeanUtils.copyProperties(dto,userProfitRule);

        userProfitRule.setAdminId(adminId);
        userProfitRule.setOperator(iAdminService.getAdminName(adminId));
        String key =LOCK_USERPROFITRULE_UPDATE+userProfitRule.getId();
        boolean b = redissonLocker.tryLock(key, TimeUnit.SECONDS, 10, 30);
        try {
            if (!b) {
                return ResponseUtil.result(30014);
            }
            if (ConstantsEnum.PROFIT_TYPE_1.integerValue().equals(dto.getProfitType())){
                JSONObject jsonObject = JSON.parseObject(userProfitRule.getContent());
                userProfitRule.setPlus(jsonObject.getBigDecimal("plus"));
            }
            iUserProfitRuleService.updateById(userProfitRule);
            return list(adminId, dto);
        }catch (Exception e){
            throw e;
        }finally {
            if(b){
                redissonLocker.unlock(key);
            }

        }
    }

    /**
     * 根据类型返回分润规则配置
     * @param adminId
     * @param type
     * @return
     */
    @GetMapping("/list/{type}")
    public Object listType(@LoginAdmin Integer adminId, @PathVariable("type") Integer type) {

        UserProfitRule userProfitRule = iUserProfitRuleService.getOne(new QueryWrapper<UserProfitRule>().eq("mode_type", type));
        if (userProfitRule == null) {
            return ResponseUtil.ok();
        }
        Map<String, Object> backMap = new HashMap<>();
        backMap.put("operator", userProfitRule.getOperator());
        backMap.put("content", JSON.parseObject(userProfitRule.getContent()));
        backMap.put("updateTime", userProfitRule.getUpdateTime());
        return ResponseUtil.ok(backMap);
    }

    /**
     * 添加新的配置
     * @param adminId
     * @param string 内容
     * @return
     */
    @PostMapping("/create")
    public Object create(@LoginAdmin Integer adminId, @RequestBody String string) {
        LogOperateUtil.log("基础课时费管理", "添加", "0", adminId.longValue(), 0);
        if (StringUtils.isBlank(string)) {
            return ResponseUtil.badArgument();
        }
        JSONObject jsonObject = JSON.parseObject(string);
        Integer type = jsonObject.getInteger(TYPE);
        if (type == null) {
            return ResponseUtil.badArgument();
        }
        jsonObject.remove(TYPE);
        iUserProfitRuleService.create(adminId, jsonObject.toJSONString(), type);
        return ResponseUtil.ok();
    }
}
