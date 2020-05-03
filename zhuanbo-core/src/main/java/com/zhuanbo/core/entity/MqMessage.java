package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * MQ处理异常消息
 * </p>
 *
 * @author rome
 * @since 2019-07-10
 */
@TableName("shop_mq_message")
@Data
public class MqMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 交换机名称
     */
    private String exchange;

    /**
     * routingKey名称
     */
    private String routingKey;

    /**
     * 消费体
     */
    private String message;

    /**
     * header参数
     */
    private String header;

    /**
     * 重试次数
     */
    private Integer times;

    /**
     * 0:待发送，1：已发送，2：已消费（客户端）
     */
    private Integer status;

    /**
     * 唯一标识符
     */
    private String uuid;

    private LocalDateTime addTime;
}
