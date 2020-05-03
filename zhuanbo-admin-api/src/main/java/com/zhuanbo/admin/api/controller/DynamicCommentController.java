package com.zhuanbo.admin.api.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.entity.DynamicComment;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.IDynamicCommentService;
import com.zhuanbo.service.utils.LogOperateUtil;
import com.zhuanbo.service.vo.DynamicCommentAdminVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 广告表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/admin/dynamic/comment")
public class DynamicCommentController {

    @Autowired
    private IDynamicCommentService iDynamicCommentService;
    @Autowired
    private IAdminService iAdminService;
    @Autowired
    private RedissonDistributedLocker redissonLocker;
    private final static String LOCK_DYNAMIC_COMMENT_UPDATE = "lock_dynamic_comment_update_";

    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId,
                       DynamicComment dynamicComment,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "-1") Integer limit) {

        IPage<DynamicCommentAdminVO> pageCond = new Page<>(page, limit);
        Map<String, Object> map = new HashMap<>();

        if (dynamicComment != null) {
            Optional.ofNullable(dynamicComment.getId()).ifPresent(x -> map.put("id", dynamicComment.getId()));
            if (StringUtils.isNotBlank(dynamicComment.getContent())) {
                map.put("content", "%" + dynamicComment.getContent() + "%");
            }
            Optional.ofNullable(dynamicComment.getChecked()).ifPresent(x -> map.put("checked", dynamicComment.getChecked()));
        }

        IPage<DynamicCommentAdminVO> iPage = iDynamicCommentService.list(pageCond, map);
        Map<String, Object> data = new HashMap<>();
        data.put("total", iPage.getTotal());
        data.put("items", iPage.getRecords());
        return ResponseUtil.ok(data);
    }

    /**
     * 审核
     *
     * @param adminId
     * @return
     */
    @PostMapping("/check")
    public Object check(@LoginAdmin Integer adminId, @RequestBody JSONObject jsonObject) {
        JSONArray id = jsonObject.getJSONArray("ids");
        if (id == null || id.size() == 0) {
            return ResponseUtil.fail("11111", "缺少参数或参数为空：ids");
        }
        LogOperateUtil.log("动态评论管理", "审核", String.valueOf(id), adminId.longValue(), 0);

        List<Integer> ids = id.toJavaList(Integer.class);
        String lockKey = LOCK_DYNAMIC_COMMENT_UPDATE + String.valueOf(id);
        boolean b = redissonLocker.tryLock(lockKey, TimeUnit.SECONDS, 10, 30);
        if (!b) {
            return ResponseUtil.result(22001);
        }
        try {
            iDynamicCommentService.update(new DynamicComment(), new UpdateWrapper<DynamicComment>().in("id", ids).set("checked", 1)
                    .set("operator", iAdminService.getAdminName(adminId)).set("update_time", LocalDateTime.now()));
            return listByIds(ids);
        } catch (Exception e) {
            throw e;
        } finally {
            if (b) {
                redissonLocker.unlock(lockKey);
            }
        }

    }

    /**
     * 上下线
     *
     * @param adminId
     * @return
     */
    @PostMapping("/upDown")
    public Object upDown(@LoginAdmin Integer adminId, @RequestBody JSONObject jsonObject) {
        LogOperateUtil.log("动态评论管理", "上下线", String.valueOf(jsonObject.getJSONArray("ids")), adminId.longValue(), 0);
        if (jsonObject.getJSONArray("ids") == null || jsonObject.getJSONArray("ids").size() == 0) {
            return ResponseUtil.fail("11111", "缺少参数：ids");
        }
        if (jsonObject.getInteger("deleted") == null) {
            return ResponseUtil.fail("11111", "缺少参数：deleted");
        }

        LocalDateTime now = LocalDateTime.now();
        if (jsonObject.getInteger("deleted").equals(0)) {// 上线
            iDynamicCommentService.update(new DynamicComment(), new UpdateWrapper<DynamicComment>()
                    .in("id", jsonObject.getJSONArray("ids").toJavaList(Integer.class))
                    .set("deleted", 0).set("checked", 1).set("operator", iAdminService.getAdminName(adminId)).set("update_time", now));
        } else {
            iDynamicCommentService.update(new DynamicComment(), new UpdateWrapper<DynamicComment>()
                    .in("id", jsonObject.getJSONArray("ids").toJavaList(Integer.class))
                    .set("deleted", 1).set("checked", 1).set("operator", iAdminService.getAdminName(adminId)).set("update_time", now));
        }
        return listByIds(jsonObject.getJSONArray("ids").toJavaList(Integer.class));
    }

    private Object listByIds(List<Integer> ids) {

        Map<String, Object> map = new HashMap<>();
        map.put("ids", ids.stream().map(x -> x.toString()).collect(Collectors.joining(",")));
        IPage<DynamicCommentAdminVO> iPage = iDynamicCommentService.list(null, map);
        Map<String, Object> data = new HashMap<>();
        data.put("total", iPage.getTotal());
        data.put("items", iPage.getRecords());
        return ResponseUtil.ok(data);
    }
}
