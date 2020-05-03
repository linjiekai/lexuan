package com.zhuanbo.admin.api.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.service.service.IFeedbackService;
import com.zhuanbo.service.vo.FeedbackVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 反馈表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2019-01-14
 */
@RestController
@RequestMapping("/admin/feedback")
public class FeedbackController {

    @Autowired
    private IFeedbackService iFeedbackService;

    /**
     * 反馈列表
     * @param
     * @return
     */
    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "-1") Integer limit,
                       Long id, String startDate, String endDate,String platform) {

        Page<FeedbackVO> pageCond = new Page<>(page, limit);

        if (StringUtils.isNotBlank(startDate)) {
            startDate = startDate + " 00:00:00";
        }
        if (StringUtils.isNotBlank(endDate)) {
            endDate = endDate+ " 23:59:59";
        }
        IPage<FeedbackVO> iPage = iFeedbackService.listMap(pageCond, id, startDate, endDate,platform);

        Map<String, Object> data = new HashMap<>();
        data.put("total", iPage.getTotal());
        data.put("items", iPage.getRecords());
        return ResponseUtil.ok(data);
    }
}
