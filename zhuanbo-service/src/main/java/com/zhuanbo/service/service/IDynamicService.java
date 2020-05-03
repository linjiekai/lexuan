package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.Dynamic;
import com.zhuanbo.service.vo.DynamicVO;

import java.time.LocalDateTime;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author rome
 * @since 2019-04-04
 */
public interface IDynamicService extends IService<Dynamic> {


    Page<DynamicVO> list(Page<DynamicVO> page, Integer userId);

    /**
     * 更新点赞数量
     * @param id
     * @param number
     * @return
     */
    int updateLikeNumber(Long id, int number);

    /**
     * 相差多长时间了，如：xxx天
     * @param time
     * @return
     */
    String toHowLongTime(LocalDateTime time);
}
