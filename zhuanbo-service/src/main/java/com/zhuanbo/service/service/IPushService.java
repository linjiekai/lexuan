package com.zhuanbo.service.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.Push;
import com.zhuanbo.service.vo.NotifyPushMQVO;
import com.zhuanbo.service.vo.PushParamVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 推送表 服务类
 * </p>
 *
 * @author rome
 * @since 2019-04-11
 */
public interface IPushService extends IService<Push> {
    /**
     * 推送功能
     * @param pushParamVO
     * @return
     */
    JSONObject push(PushParamVO pushParamVO);

    /**
     * 根据taskId查看任务状态
     * @param taskId
     * @return
     */
    JSONObject taskStatus(String taskId, String platform);

    /**
     * 取消任务，0-排队中, 1-发送中
     * @param taskId
     * @return
     */
    JSONObject taskCancel(String taskId, String platform);

    /**
     * 普通推送
     * @param title 标题
     * @param subTitle 副标题
     * @param extra 扩展内容
     * @param userIds 目标ids
     * @param platform 平台
     * @param catchException 是否获取异常
     */
    JSONObject simplePush(String title, String subTitle, Map<String, Object> extra, List<Long> userIds, String platform, boolean catchException);

    /**
     * 推送
     * @param exchange
     * @param routingKey
     * @param notifyPushMQVOList
     */
    void push(String exchange, String routingKey, List<NotifyPushMQVO> notifyPushMQVOList);

    /**
     * 推送（+入库，看action）
     * @param notifyPushMQVO
     */
    void doByAction(NotifyPushMQVO notifyPushMQVO);
}
