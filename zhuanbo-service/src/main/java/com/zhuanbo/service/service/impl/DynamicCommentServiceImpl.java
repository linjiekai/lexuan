package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.DynamicComment;
import com.zhuanbo.service.mapper.DynamicCommentMapper;
import com.zhuanbo.service.service.IDynamicCommentService;
import com.zhuanbo.service.vo.DynamicCommentAdminVO;
import com.zhuanbo.service.vo.DynamicCommentVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 动态评论表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-04-08
 */
@Service
public class DynamicCommentServiceImpl extends ServiceImpl<DynamicCommentMapper, DynamicComment> implements IDynamicCommentService {

    @Override
    public Page<DynamicCommentVO> list(Page<DynamicCommentVO> page,Integer dynamicId,  Integer commentId) {
        page.setRecords(baseMapper.list(page,dynamicId, commentId));
        return page;
    }

    @Override
    public IPage<DynamicCommentAdminVO> list(IPage<DynamicCommentAdminVO> page, Map<String, Object> map) {
        List<DynamicCommentAdminVO> list = baseMapper.adminList(page, map);
        if (page == null) {
            page = new Page<>();
            page.setTotal(list.size());
        }
        if (page.getSize() == -1) {
            page.setTotal(list.size());
        }
        page.setRecords(list);
        return page;
    }

}
