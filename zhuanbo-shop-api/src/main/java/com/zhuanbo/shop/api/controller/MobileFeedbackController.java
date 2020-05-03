package com.zhuanbo.shop.api.controller;


import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.core.util.ResponseUtil;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.entity.Feedback;
import com.zhuanbo.service.service.IFeedbackService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * <p>
 * 反馈表 前端控制器
 * </p>
 *
 * @author rome
 * @since 2019-01-14
 */
@RestController
@RequestMapping("/shop/mobile/feedback")
public class MobileFeedbackController {

    @Autowired
    private IFeedbackService iFeedbackService;

    /**
     * 创建反馈
     * @param feedback
     * @return
     */
    @PostMapping("/create")
    public Object create(HttpServletRequest request, @RequestBody Feedback feedback) {

        if (StringUtils.isBlank(feedback.getContent())) {
            return ResponseUtil.fail("11111", "缺少参数:content");
        }

        String token = request.getHeader(Constants.LOGIN_TOKEN_KEY);
        if (StringUtils.isBlank(token)) {
            feedback.setUserId(0);
        } else {
            feedback.setUserId(RedisUtil.get(token) == null ? 0 : Integer.parseInt(RedisUtil.get(token).toString()));
        }
        feedback.setStatus(false);// 用户提交
        feedback.setUpdateTime(LocalDateTime.now());
        feedback.setMobileModel(request.getHeader("X-zhuanbo-DeviceModel"));
        feedback.setMobileSystemVersion(request.getHeader("X-zhuanbo-OSVer"));
        feedback.setAppVersion(request.getHeader("X-MPMALL-APPVer"));
        feedback.setNetwork(request.getHeader("X-zhuanbo-Network"));
        iFeedbackService.save(feedback);
        return ResponseUtil.ok();
    }

}
