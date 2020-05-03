package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.NotifyMsg;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.service.vo.NotifyPushMQVO;

import java.util.Map;


/**
 * <p>
 * 通知消息表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
public interface INotifyMsgService extends IService<NotifyMsg> {

    void simpleSave(User user, String platform, Integer msgFlag, String title, String content);

    /**
     * 保存通知且推送
     * @param user 用户
     * @param platform 平台
     * @param msgFlag 消息标记 1:系统通知
     * @param title 通知标题
     * @param content 内容
     * @param m 携带内容
     */
    void notifyAndPush(User user, String platform, Integer msgFlag, String title, String content, Map<String, Object> m);

    /**
     *
     * @param notifyPushMQVO
     */
    void save(NotifyPushMQVO notifyPushMQVO);
}
