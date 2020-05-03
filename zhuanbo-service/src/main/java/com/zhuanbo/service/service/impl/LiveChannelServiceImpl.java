package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.entity.LiveChannel;
import com.zhuanbo.service.mapper.LiveChannelMapper;
import com.zhuanbo.service.service.ILiveChannelService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 直播频道创建表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-03-15
 */
@Service
public class LiveChannelServiceImpl extends ServiceImpl<LiveChannelMapper, LiveChannel> implements ILiveChannelService {

    @Override
    public int updateOnLineUserNumberUp(String cId) {
        return baseMapper.updateOnLineUserNumberUp(cId);
    }

    @Override
    public int updateOnLineUserNumberDown(String cId) {
        return baseMapper.updateOnLineUserNumberDown(cId);
    }
}
