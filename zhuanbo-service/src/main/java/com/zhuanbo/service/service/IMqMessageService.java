package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.MqMessage;
import org.springframework.amqp.core.Message;

import java.util.Map;

/**
 * <p>
 * MQ处理异常消息 服务类
 * </p>
 *
 * @author rome
 * @since 2019-07-10
 */
public interface IMqMessageService extends IService<MqMessage> {
    /**
     * 简单保存
     * @param exchange
     * @param routingKey
     * @param message
     * @param header
     */
    void simpleSave(String exchange, String routingKey, String message, Map<String, Object> header);

    /**
     * 客户端拿到消息后，判断次数后是否入库或重发
     * @param message
     */
    void tryOrStore(Message message) throws Exception;

    /**
     * 更新状态
     * @param status
     * @param uuid
     * @return
     */
    int updateStatusByUUID(Integer status, String uuid);

    void n2Save(Integer times, String exchange, String message, String routingKey, String uuid);
}
