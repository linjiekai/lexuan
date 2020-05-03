package com.zhuanbo.shop.api.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhuanbo.core.config.AuthConfig;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.ReqResEnum;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.HttpUtil;
import com.zhuanbo.core.util.JacksonUtil;
import com.zhuanbo.core.util.LogUtil;
import com.zhuanbo.service.service.IMqMessageService;
import com.zhuanbo.service.service.IUserService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单完成后的处理
 */

@Component
@Slf4j
public class LiveUserReceiver {

	@Autowired
	private IMqMessageService iMqMessageService;
	@Autowired
	private IUserService iUserService;

	@Autowired
	private AuthConfig authConfig;

	// 用来标识MQ状态 1：开启 0：关闭
	public static Integer MQ_STATUS = 0;

	@RabbitListener(autoStartup = "${mq.listener.switch}",bindings = @QueueBinding(value = @Queue(value = "${spring.rabbitmq.queues.live-user.queue}", durable = "true"),
			exchange = @Exchange(value = "${spring.rabbitmq.exchange}", type = ExchangeTypes.TOPIC),
			key = "${spring.rabbitmq.queues.live-user.routing-key}"), concurrency = "3")
	public void process(String msg, Channel channel, Message message) throws Exception {

		LogUtil.SHARE_PROFIT.info("同步用户数据到直播[{}]", new String(message.getBody(), "UTF-8"));

		try {
			if (Constants.MQ_SWITCH == 0) {
				MQ_STATUS = 0;
				iMqMessageService.tryOrStore(message);
				return;
			}
			MQ_STATUS = 1;

			Map<String, Object> data = JSON.parseObject(new String(message.getBody(), "UTF-8"),
					new TypeReference<Map<String, Object>>() {
					});

			if (data.get("userId") == null) {
				log.error("同步用户数据live直播失败:{}", data);
				return;
			}

			User user = iUserService.getById(data.get("userId").toString());

			if (user == null) {
				log.error("同步用户数据live直播失败:{}", data);
				return;
			}
			
			// 同步user数据到live
			Map<String, Object> request = new HashMap<String, Object>();
			request.put("mercId", authConfig.getMercId());
			request.put("userId", user.getId());
			request.put("nickname", user.getNickname());
			request.put("headImgUrl", user.getHeadImgUrl());
			request.put("mobile", user.getMobile());
			LogUtil.SHARE_PROFIT.info("同步用户数据到直播request[{}]", request);
			String result = HttpUtil.sendPostJson(authConfig.getLiveUrl() + "/user/addOrUpdate", request, null);
			
			log.info("同步用户直播数据result：{}", result);
			
			if (StringUtils.isBlank(result)) {
				throw new ShopException(10502);
			}

			JSONObject json = JSONObject.parseObject(result);
			if (!ReqResEnum.C_10000.String().equalsIgnoreCase(json.getString(ReqResEnum.CODE.String()))) {
				log.error("请求接口失败,params[{}],response[{}]", JacksonUtil.objTojson(request), json);
				throw new ShopException(json.get("code").toString(), json.get("msg").toString());
			} else {
				json = json.getJSONObject(ReqResEnum.DATA.String());

				Long liveUserId = json.getLongValue("liveUserId");

				iUserService.update(new User(),
						new UpdateWrapper<User>().set("live_user_id", liveUserId).eq("id", user.getId()));
			}

		} catch (Exception e) {
			log.error("MQ处理支付完成订单失败:{}", e);

			iMqMessageService.tryOrStore(message);
		} finally {
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

			LogUtil.SHARE_PROFIT.info("No:{} MQ开始分润，释放锁", msg);
		}

		MQ_STATUS = 0;
	}
}
