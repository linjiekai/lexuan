package com.zhuanbo.admin.api.mq;


import com.zhuanbo.service.service.IMqMessageService;
import com.zhuanbo.service.service.IVideoTransCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VideoTransCodeReceiver {

    @Autowired
    private IMqMessageService iMqMessageService;
    @Autowired
    private IVideoTransCodeService iVideoTransCodeService;

    /**
     * 视频转码
     * @param message
     * @param headers
     * @param channel
     * @throws Exception
     */
    /*@RabbitListener(bindings = @QueueBinding(value = @Queue(value = "${spring.rabbitmq.queues.video-transcode.queue}", durable = "true"),
            exchange = @Exchange(value = "${spring.rabbitmq.exchange}", type = ExchangeTypes.TOPIC),
            key = "${spring.rabbitmq.queues.video-transcode.routing-key}") ,concurrency = "2")
    public void videoTransCode(@Payload Message message, @Headers Map<String, Object> headers, Channel channel) throws Exception {

        try {
            String s = new String(message.getBody(), "UTF-8");
            TransCodeVO transCodeVO = JSON.parseObject(s, TransCodeVO.class);
            transCodeVO = iVideoTransCodeService.transCode(transCodeVO);

            if (!transCodeVO.getResult().equals(0)) {// 未处理完，重新发送
                log.info("转码未完成:{}", transCodeVO);
                iVideoTransCodeService.sendTrans(transCodeVO);
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("MQ:videoTransCode:失败:{}", e);
            iMqMessageService.tryOrStore(message);
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }*/
}
