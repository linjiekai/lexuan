package com.zhuanbo.admin.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.service.service.IUserService;
/*import com.zhuanbo.service.service3rd.arangodb.bean.GUser;
import com.zhuanbo.service.service3rd.arangodb.service.IGraphService;
import com.zhuanbo.service.service3rd.arangodb.vo.UTree;*/
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 团队
 */
@RestController
@RequestMapping("/admin/team")
public class TeamController {

    /*@Autowired
    private IGraphService iGraphService;*/
    @Autowired
    private IUserService iUserService;

    /**
     * 列表
     * @param
     * @return
     */
    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "-1") Integer limit) {
        /*if (StringUtils.isBlank(gUser.getId())) {
            gUser.setId("1");
        }
        User user = iUserService.getById(Long.valueOf(gUser.getId()));
        List<UTree> uTreeList = iGraphService.treeAdminFirst(user);*/

        Map<String, Object> data = new HashMap<>();
        data.put("total", 0);
        data.put("items", Lists.newArrayList());
        return ResponseUtil.ok(data);
    }

    /**
     * 搜索
     * @param
     * @return
     */
    @GetMapping("/list/search")
    public Object listSearch(@LoginAdmin Integer adminId,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "-1") Integer limit,
                       User user) throws Exception {


        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        Optional.ofNullable(StringUtils.trimToNull(user.getNickname())).ifPresent(x -> userQueryWrapper.eq("nickname", x));
        Optional.ofNullable(user.getId()).ifPresent(x -> userQueryWrapper.eq("id", x));
        Optional.ofNullable(StringUtils.trimToNull(user.getMobile())).ifPresent(x -> userQueryWrapper.eq("mobile", x));
        userQueryWrapper.eq("deleted", 0);

        List<User> list = iUserService.list(userQueryWrapper);
        //List<UTree> uTreeList = iGraphService.parentTree(list);

        Map<String, Object> data = new HashMap<>();
        data.put("total", 0L);
        data.put("items", Lists.newArrayList());
        return ResponseUtil.ok(data);
    }
}
