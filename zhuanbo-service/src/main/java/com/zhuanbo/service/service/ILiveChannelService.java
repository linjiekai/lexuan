package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.LiveChannel;

/**
 * <p>
 * 直播频道创建表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-03-15
 */
public interface ILiveChannelService extends IService<LiveChannel> {

    /**
     * 增加在线人数
     * @param cId
     * @return
     */
    int updateOnLineUserNumberUp(String cId);

    /**
     * 减少在线人数
     * @param cId
     * @return
     */
    int updateOnLineUserNumberDown(String cId);
}
