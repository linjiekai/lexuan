package com.zhuanbo.admin.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.service.impl.RedissonDistributedLocker;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.CommonQuestion;
import com.zhuanbo.service.service.IAdminService;
import com.zhuanbo.service.service.ICommonQuestionService;
import com.zhuanbo.service.utils.LogOperateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 问题
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/admin/question")
public class QuestionController {

    @Autowired
    private ICommonQuestionService iCommonQuestionService;
    @Autowired
    private IAdminService iAdminService;
    @Autowired
    private RedissonDistributedLocker redissonLocker;
    private final static String LOCK_QUESTION_UPDATE = "lock_question_update_";


    /**
     * 列表
     *
     * @param adminId
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       CommonQuestion commonQuestion) {

        IPage<CommonQuestion> iPage = new Page<>(page, limit);
        QueryWrapper<CommonQuestion> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", ConstantsEnum.DELETED_0.integerValue());
        queryWrapper.orderByDesc("serial_number");
        if (commonQuestion.getId() != null) {
            queryWrapper.eq("id", commonQuestion.getId());
        }
        if (StringUtils.isNotBlank(commonQuestion.getPosition())) {
            queryWrapper.eq("position", commonQuestion.getPosition());
        }

        IPage<CommonQuestion> list = iCommonQuestionService.page(iPage, queryWrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("total", list.getTotal());
        data.put("items", list.getRecords());
        return ResponseUtil.ok(data);
    }

    /**
     * 添加
     *
     * @param adminId
     * @param commonQuestion
     * @return
     */
    @PostMapping("/create")
    public Object create(@LoginAdmin Integer adminId, @RequestBody CommonQuestion commonQuestion) {
        LogOperateUtil.log("问题管理", "创建", null, adminId.longValue(), 0);
        if (StringUtils.isBlank(commonQuestion.getPlatform()) || StringUtils.isBlank(commonQuestion.getPosition())
                || StringUtils.isBlank(commonQuestion.getQuestion())
                || StringUtils.isBlank(commonQuestion.getAnswer())) {
            return ResponseUtil.fail("11111", "缺少参数：platform或position或question或answer");
        }

        LocalDateTime now = LocalDateTime.now();
        commonQuestion.setAddTime(now);
        commonQuestion.setUpdateTime(now);
        commonQuestion.setOperator(iAdminService.getAdminName(adminId));
        iCommonQuestionService.save(commonQuestion);

        CommonQuestion commonQuestion1 = new CommonQuestion();
        commonQuestion1.setId(commonQuestion.getId());
        return list(adminId, 1, 1, commonQuestion);
    }

    /**
     * 详情
     *
     * @param adminId
     * @param id
     * @return
     */
    @GetMapping("/detail/{id}")
    public Object detail(@LoginAdmin Integer adminId, @PathVariable("id") Integer id) {
        return ResponseUtil.ok(iCommonQuestionService.getById(id));
    }

    /**
     * 更新
     *
     * @param adminId
     * @param commonQuestion
     * @return
     */
    @PostMapping("/update")
    public Object update(@LoginAdmin Integer adminId, @RequestBody CommonQuestion commonQuestion) throws Exception {
        LogOperateUtil.log("问题管理", "修改", String.valueOf(commonQuestion.getId()), adminId.longValue(), 0);
        if (commonQuestion.getId() == null || StringUtils.isBlank(commonQuestion.getPlatform()) || StringUtils.isBlank(commonQuestion.getPosition())
                || StringUtils.isBlank(commonQuestion.getQuestion())
                || StringUtils.isBlank(commonQuestion.getAnswer())) {
            return ResponseUtil.fail("11111", "缺少参数：id或platform或position或question或answer");
        }

        LocalDateTime now = LocalDateTime.now();
        commonQuestion.setUpdateTime(now);
        commonQuestion.setOperator(iAdminService.getAdminName(adminId));
        updateByLock(commonQuestion);
        CommonQuestion commonQuestion1 = new CommonQuestion();
        commonQuestion1.setId(commonQuestion.getId());
        return list(adminId, 1, 1, commonQuestion);
    }

    @PostMapping("/del")
    public Object del(@LoginAdmin Integer adminId, @RequestBody CommonQuestion commonQuestion) throws Exception{
        LogOperateUtil.log("问题管理", "删除", String.valueOf(commonQuestion.getId()), adminId.longValue(), 0);
        if (commonQuestion.getId() != null) {
            commonQuestion = iCommonQuestionService.getById(commonQuestion.getId());
            if (commonQuestion != null) {
                commonQuestion.setDeleted(ConstantsEnum.DELETED_1.integerValue());
                commonQuestion.setUpdateTime(LocalDateTime.now());
                commonQuestion.setOperator(iAdminService.getAdminName(adminId));
                updateByLock(commonQuestion);
            }
        }
        return ResponseUtil.ok();
    }

   public void updateByLock(CommonQuestion commonQuestion) throws Exception {
        String lockKey = LOCK_QUESTION_UPDATE + commonQuestion.getId();
        boolean b = redissonLocker.tryLock(lockKey, TimeUnit.SECONDS, 10, 30);
        if (!b) {
            throw new ShopException(30014);
        }
        try {
            iCommonQuestionService.updateById(commonQuestion);
        } catch (Exception e) {
            throw e;
        } finally {
            if (b) {
                redissonLocker.unlock(lockKey);
            }
        }
    }
}
