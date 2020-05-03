package com.zhuanbo.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.core.entity.NotifyMsgPool;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.service.mapper.NotifyMsgPoolMapper;
import com.zhuanbo.service.service.INotifyMsgPoolService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 通知消息池表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-06-14
 */
@Service
public class NotifyMsgPoolServiceImpl extends ServiceImpl<NotifyMsgPoolMapper, NotifyMsgPool> implements INotifyMsgPoolService {

    @Override
    public void simpleSave(User user, String platform, Integer msgType, String content) {

        LocalDateTime now = LocalDateTime.now();
        NotifyMsgPool notifyMsgPool = new NotifyMsgPool();
        notifyMsgPool.setUserId(user.getId());
        notifyMsgPool.setPtLevel(user.getPtLevel());
        notifyMsgPool.setNickname(user.getNickname());
        notifyMsgPool.setHeadImgUrl(user.getHeadImgUrl());
        notifyMsgPool.setPlatform(platform);
        notifyMsgPool.setMsgType(msgType);
        notifyMsgPool.setStatus(1);
        notifyMsgPool.setContent(content);
        notifyMsgPool.setMsgDate(DateUtil.toyyyy_MM_dd(now));
        notifyMsgPool.setMsgTime(DateUtil.toHH_mm_ss(now));
        notifyMsgPool.setAddTime(now);
        notifyMsgPool.setUpdateTime(now);
        save(notifyMsgPool);
    }
}
