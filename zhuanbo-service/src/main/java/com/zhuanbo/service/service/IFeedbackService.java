package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.Feedback;
import com.zhuanbo.service.vo.FeedbackVO;

/**
 * <p>
 * 反馈表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-01-14
 */
public interface IFeedbackService extends IService<Feedback> {

    /**
     * 管理后台列表
     * @param id
     * @param startDate
     * @param endDate
     * @return
     */
    Page<FeedbackVO> listMap(Page<FeedbackVO> page, Long id, String startDate, String endDate, String platform);
}
