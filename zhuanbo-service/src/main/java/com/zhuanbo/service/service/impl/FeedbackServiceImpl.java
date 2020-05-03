package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.Feedback;
import com.zhuanbo.service.mapper.FeedbackMapper;
import com.zhuanbo.service.service.IFeedbackService;
import com.zhuanbo.service.vo.FeedbackVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 反馈表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-01-14
 */
@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements IFeedbackService {

    @Override
    public Page<FeedbackVO> listMap(Page<FeedbackVO> page, Long id, String startDate, String endDate,String platform) {

        List<FeedbackVO> feedbackVOS = baseMapper.listMap(page, id, startDate, endDate,platform);
        if (page == null) {
            page = new Page<>();
            page.setTotal(feedbackVOS.size());
        }
        if (page.getSize() == -1) {
            page.setTotal(feedbackVOS.size());
        }
        page.setRecords(feedbackVOS);
        return page;
    }
}
