package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.NotifyMsgPool;
import com.zhuanbo.core.entity.User;

/**
 * <p>
 * 通知消息池表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-06-14
 */
public interface INotifyMsgPoolService extends IService<NotifyMsgPool> {

    void simpleSave(User user, String platform, Integer msgType, String content);
}
