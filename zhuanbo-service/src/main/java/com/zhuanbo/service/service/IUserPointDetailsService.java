package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.dto.UserPointDetailsDTO;
import com.zhuanbo.core.entity.UserPointDetails;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户积分明细表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
public interface IUserPointDetailsService extends IService<UserPointDetails> {

    /**
     * 分页查询积分变动明细
     *
     * @param userPointDetailsDTO
     * @param iPage
     * @return
     */
    List<UserPointDetailsDTO> page(IPage iPage, UserPointDetailsDTO userPointDetailsDTO);

    /**
     * 统计积分
     * 1.只针对管理员用户进行展示
     * 2.总充值积分=管理员给联创充值的积分总和
     * 已使用积分=所有联创扣减的积分总和
     * 剩余积分=总充值积分 - 已使用积分
     *
     * @return
     */
    Map<String, Object> statisticPoint();

    /**
     * 积分日统计
     *
     * @return
     */
    Map<String, Object> statisticPointByDay(UserPointDetailsDTO userPointDetailsDTO);
}
