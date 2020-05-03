package com.zhuanbo.admin.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.dto.AdminUserPartnerProfitRuleDTO;
import com.zhuanbo.core.entity.UserPartnerProfitRule;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.IUserPartnerProfitRuleService;
import com.zhuanbo.service.utils.LogOperateUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 运营商利润分配规则表  （运营商基础课时费规则）
 */
@RestController
@RequestMapping("/admin/userPartner/profit/rule")
public class UserOperProfitRuleController {

    @Autowired
    private IUserPartnerProfitRuleService iUserPartnerProfitRuleService;
    @Autowired
    private IAdminService iAdminService;
    @Autowired
    private RedissonDistributedLocker redissonLocker;
    private final static String LOCK_USEROPERPROFITRULE_UPDATE_ = "lock_userpartnerprofitrule_update_";
    /**
     * 列表
     * @return
     */
    @PostMapping("/list")
    public Object list(@LoginAdmin Integer adminId,@RequestBody AdminUserPartnerProfitRuleDTO dto) {
        IPage<UserPartnerProfitRule> iPage = new Page<>(dto.getPage(), dto.getLimit());
        QueryWrapper<UserPartnerProfitRule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("profit_type", dto.getProfitType().intValue());
        IPage<UserPartnerProfitRule> page = iUserPartnerProfitRuleService.page(iPage, queryWrapper);

        Map<String, Object> backMap = new HashMap<>();
        backMap.put("total", page.getTotal());
        backMap.put("items", page.getRecords());
        return ResponseUtil.ok(backMap);
    }

    @PostMapping("/update")
    public Object update(@LoginAdmin Integer adminId, @RequestBody AdminUserPartnerProfitRuleDTO dto) {
        LogOperateUtil.log("合伙人课时费管理", "修改", String.valueOf(dto.getId()), adminId.longValue(), 0);

        UserPartnerProfitRule userProfitRule = new UserPartnerProfitRule();
        BeanUtils.copyProperties(dto,userProfitRule);

        userProfitRule.setAdminId(adminId);
        userProfitRule.setOperator(iAdminService.getAdminName(adminId));
        String key =LOCK_USEROPERPROFITRULE_UPDATE_+userProfitRule.getId();
        boolean b = redissonLocker.tryLock(key, TimeUnit.SECONDS, 10, 30);
        try {
            if (!b) {
                return ResponseUtil.result(30014);
            }
            iUserPartnerProfitRuleService.updateById(userProfitRule);
            return list(adminId, dto);
        }catch (Exception e){
            throw e;
        }finally {
            if(b){
                redissonLocker.unlock(key);
            }

        }
    }
}
