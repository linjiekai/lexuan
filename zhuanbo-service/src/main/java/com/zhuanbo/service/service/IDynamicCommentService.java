package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.DynamicComment;
import com.zhuanbo.service.vo.DynamicCommentAdminVO;
import com.zhuanbo.service.vo.DynamicCommentVO;

import java.util.Map;

/**
 * <p>
 * 动态评论表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-04-08
 */
public interface IDynamicCommentService extends IService<DynamicComment> {

    Page<DynamicCommentVO> list(Page<DynamicCommentVO> page, Integer dynamicId, Integer commentId);

    /**
     * 管理后台
     * @param page
     * @return
     */
    IPage<DynamicCommentAdminVO> list(IPage<DynamicCommentAdminVO> page, Map<String, Object> map);

}
