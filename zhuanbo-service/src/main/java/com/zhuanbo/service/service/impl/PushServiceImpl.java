package com.zhuanbo.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanbo.core.config.PushConfig;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.Push;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.util.DateUtil;
import com.zhuanbo.service.mapper.PushMapper;
import com.zhuanbo.service.service.INotifyMsgService;
import com.zhuanbo.service.service.IPushService;
import com.zhuanbo.service.service.IUserService;
import com.zhuanbo.service.vo.NotifyPushMQVO;
import com.zhuanbo.service.vo.PushParamVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * <p>
 * 推送表 服务实现类
 * </p>
 *
 * @author rome
 * @since 2019-04-11
 */
@Service
@Slf4j
public class PushServiceImpl extends ServiceImpl<PushMapper, Push> implements IPushService {

    public static final String PUSH_PREFIX = "zhuanbo_";

    @Autowired
    private PushConfig pushConfig;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private INotifyMsgService iNotifyMsgService;
    @Autowired
    private IUserService iUserService;

    @Override
    public JSONObject push(PushParamVO pushParamVO) {

        Map<String, Object> paramMap = toMap(pushParamVO);
        Map<String, Object> checkMap = checkMap(paramMap);
        PushConfig.PConfig pConfig;
        if (ConstantsEnum.PLATFORM_ZBMALL.stringValue().equals(pushParamVO.getPlatform())) {
            pConfig = pushConfig.getZbmall();
        } else {
            pConfig = pushConfig.getZblmall();
        }
        checkMap.put("X-PUSH-AppVer", pConfig.getVersion());
        return postResult(pConfig, pConfig.getUrlPush(), checkMap, paramMap);
    }

    @Override
    public JSONObject taskStatus(String taskId, String platform) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("taskId", taskId);

        PushConfig.PConfig pConfig;
        if (ConstantsEnum.PLATFORM_ZBMALL.stringValue().equals(platform)) {
            pConfig = pushConfig.getZbmall();
        } else {
            pConfig = pushConfig.getZblmall();
        }
        Map<String, Object> checkMap = new HashMap<>();
        checkMap.put("taskId", taskId);
        checkMap.put("X-PUSH-AppVer", pConfig.getVersion());

        return postResult(pConfig, pConfig.getUrlStatus(), checkMap, paramMap);
    }

    @Override
    public JSONObject taskCancel(String taskId, String platform) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("taskId", taskId);

        Map<String, Object> checkMap = new HashMap<>();
        checkMap.put("taskId", taskId);

        PushConfig.PConfig pConfig;
        if (ConstantsEnum.PLATFORM_ZBMALL.stringValue().equals(platform)) {
            pConfig = pushConfig.getZbmall();
        } else {
            pConfig = pushConfig.getZblmall();
        }
        checkMap.put("X-PUSH-AppVer", pConfig.getVersion());

        return postResult(pConfig, pConfig.getUrlCancel(), checkMap, paramMap);
    }

    @Override
    public JSONObject simplePush(String title, String subTitle, Map<String, Object> extra, List<Long> userIds, String platform, boolean catchException) {

       if (catchException) {
           try {
               return sp(title, subTitle, extra, userIds, platform);
           } catch (Exception e) {
               log.error("推送失败：{}", e);
           }
           return null;
       } else {
           return sp(title, subTitle, extra, userIds, platform);
       }
    }

    @Override
    public void push(String exchange, String routingKey, List<NotifyPushMQVO> notifyPushMQVOList) {// mq引用到

        if (!CollectionUtils.isEmpty(notifyPushMQVOList)) {
            for (NotifyPushMQVO notifyPushMQVO : notifyPushMQVOList) {
                rabbitTemplate.convertAndSend(exchange, routingKey, JSON.toJSONString(notifyPushMQVO));
            }
        }
    }

    @Override
    public void doByAction(NotifyPushMQVO notifyPushMQVO) {

        if (StringUtils.isNotBlank(notifyPushMQVO.getAction())) {
            doNotify(notifyPushMQVO);
        }
        simplePush(notifyPushMQVO.getTitle(), notifyPushMQVO.getContent(), notifyPushMQVO.getExtra(), Arrays.asList(notifyPushMQVO.getUserId()), notifyPushMQVO.getPlatform(), true);
    }

    public JSONObject sp(String title, String subTitle, Map<String, Object> extra, List<Long> userIds, String platform) {
        // 推送
        PushConfig.PConfig config;
        if (ConstantsEnum.PLATFORM_ZBMALL.stringValue().equalsIgnoreCase(platform)) {
            config = pushConfig.getZbmall();
        } else {
            config = pushConfig.getZblmall();
        }

        PushParamVO.PushParamVOBuilder builder = PushParamVO.builder();
        builder.productionMode(config.getProductionMode()).title(title)
                .extra(extra).ticker(subTitle).text(subTitle).subtitle(subTitle)
                .body("").mipush(config.getMipush()).miActivity(config.getMiActivity())
                .startTime(DateUtil.toyyyy_MM_dd_HH_mm_ss(LocalDateTime.now())).url(config.getUrl())
                .activity(config.getActivity()).afterOpen(config.getAfterOpen()).platform(platform);
        if (userIds == null || userIds.size() == 0) {// 全部推
            builder.type(config.getTypeBroadcast());
        } else {// 部分推（赚播是加前缀的）
            String pushUids = userIds.stream().map(x -> PUSH_PREFIX + x).collect(Collectors.joining(","));
            builder.type(config.getTypeCustomizedcas()).aliasType(config.getAliasTypeAlias()).alias(pushUids);
        }
        JSONObject push = push(builder.build());
        log.info("simplePush推送结果：{}", push);
        return push;
    }

    /**
     * bean to map
     * @param pushParamVO
     * @return
     */
    public Map<String, Object> toMap(PushParamVO pushParamVO){

        BeanMap beanMap = BeanMap.create(pushParamVO);
        Map<String, Object> map =new HashMap();
        for (Object key : beanMap.keySet()) {
            map.put(String.valueOf(key), beanMap.get(String.valueOf(key)));
        }
        return map;
    }
    /**
     * 验签前整理数据
     * @param map
     * @return
     */
    static Map<String, Object> checkMap(Map<String, Object> map){
        Map<String, Object> m = new TreeMap<>();
        m.putAll(map);
        m.entrySet().removeIf(x -> removeInvaildValue(x.getValue()));
        return m;
    }

    /**
     * 过滤验签的参数
     * @param obj
     * @return
     */
    static boolean removeInvaildValue(Object obj){
        if (obj == null || StringUtils.isBlank(obj.toString())) {
            return true;
        }
        if (obj instanceof String || obj instanceof Integer || obj instanceof Long || obj instanceof Float || obj instanceof Double) {
            return false;
        }
        return true;
    }

    /**
     * 签名
     * @param plain
     * @return
     */
    static String sign(String plain){
        return Base64.encodeBase64String(DigestUtils.md5Hex(plain).toLowerCase().getBytes());
    }

    /**
     * 请求结果
     * @param checkMap 验签的map
     * @param paramMap 参数的map
     * @return
     */
    JSONObject postResult(PushConfig.PConfig pConfig, String url, Map<String, Object> checkMap, Map<String, Object> paramMap){
        log.info("推送参数：checkMap:{}", checkMap);
        log.info("推送参数：paramMap:{}", paramMap);
        // 签名前url
        String plain = checkMap.entrySet().stream().map(x -> x.getKey() + "=" + x.getValue()).collect(Collectors.joining("&"));
        plain = plain + "&secret=" + pConfig.getSecret();
        String sign = sign(plain);// 签名后的数据

        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("X-PUSH-Utime", (System.currentTimeMillis() / 1000) + "");
        httpPost.setHeader("X-PUSH-APPVer", pConfig.getVersion());
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("X-PUSH-Sign", sign);
        httpPost.setHeader("X-PUSH-AppKey",pConfig.getAppKey());

        StringEntity entity = new StringEntity(JSON.toJSONString(paramMap), "UTF8");
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httpPost.setEntity(entity);
        // 超时设置
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(60000).setConnectionRequestTimeout(1000)
                .setSocketTimeout(60000).build();
        httpPost.setConfig(requestConfig);
        try (CloseableHttpClient client = HttpClients.createDefault(); CloseableHttpResponse response = client.execute(httpPost)) {
            JSONObject jsonObject = JSONObject.parseObject(EntityUtils.toString(response.getEntity()));
            log.info("推送结果|{}", jsonObject);
            return jsonObject;
        } catch (Exception e) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", -1);
            return jsonObject;
        }
    }

    /**
     * 消息入库
     * @param notifyPushMQVO
     */
    private void doNotify(NotifyPushMQVO notifyPushMQVO) {
        User u = iUserService.getById(notifyPushMQVO.getUserId());
        iNotifyMsgService.simpleSave(u, ConstantsEnum.PLATFORM_ZBMALL.stringValue(), 1, notifyPushMQVO.getTitle(), notifyPushMQVO.getContent());
    }
}
