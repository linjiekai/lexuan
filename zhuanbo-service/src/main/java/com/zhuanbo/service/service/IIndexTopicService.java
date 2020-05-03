package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.IndexTopic;
import com.zhuanbo.service.vo.TopicsVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author rome
 * @since 2019-03-14
 */
public interface IIndexTopicService extends IService<IndexTopic> {
    Page<TopicsVO> listTopics(Page<TopicsVO> page, String type, String goodsId);

    void refreshAllCache() throws  Exception;
}
