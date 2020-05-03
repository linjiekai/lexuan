package com.zhuanbo.admin.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.ImmutableMap;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.entity.AdjustAccount;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IAdjustAccountService;
import com.zhuanbo.service.vo.AdjustAccountVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin/adjust/account")
public class AdjustAccountController {

    @Autowired
    private IAdjustAccountService iAdjustAccountService;

    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       Long userId, String nickname, String orderNo) {

        Map<String, Object> params = new HashMap<>();
        Optional.ofNullable(userId).ifPresent(x -> params.put("userId", userId));
        if (StringUtils.isNotBlank(nickname)) {
            params.put("nickname", nickname + "%");
        }
        if (StringUtils.isNotBlank(orderNo)) {
            params.put("orderNo", orderNo + "%");
        }

        Page<AdjustAccountVO> adjustAccountVOPage = new Page<>(page, limit);
        Page<AdjustAccountVO> list = iAdjustAccountService.list(adjustAccountVOPage, params);

        ImmutableMap<String, Object> build = new ImmutableMap.Builder<String, Object>()
                .put("total", list.getTotal())
                .put("items", list.getRecords()).build();
        return ResponseUtil.ok(build);
    }

    /**
     * 添加
     * @param adminId
     * @param adjustAccount
     * @return
     * @throws Exception 
     */
    @PostMapping("/add")
    public Object add(@LoginAdmin Integer adminId, @RequestBody AdjustAccount adjustAccount) throws Exception {
        iAdjustAccountService.addOne(adminId, adjustAccount);
        return ResponseUtil.ok();
    }
}
