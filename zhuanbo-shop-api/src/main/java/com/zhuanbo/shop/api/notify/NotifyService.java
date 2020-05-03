package com.zhuanbo.shop.api.notify;

import com.zhuanbo.core.notify.NotifyType;
import com.zhuanbo.core.notify.SmsResult;
import com.zhuanbo.core.notify.SmsSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 通知服务类
 */
@Service
public class NotifyService {
    private String sendFrom;
    private String sendTo;

    private SmsSender smsSender;
    private List<Map<String, String>> smsTemplate = new ArrayList<>();

    private WxTemplateSender wxTemplateSender;
    private List<Map<String, String>> wxTemplate = new ArrayList<>();

    /**
     * 短信消息通知
     *
     * @param phoneNumber 接收通知的电话号码
     * @param message     短消息内容，这里短消息内容必须已经在短信平台审核通过
     */
    @Async
    public void notifySms(String phoneNumber, String message) {

        smsSender.send(phoneNumber, message);
    }

    /**
     * 短信模版消息通知
     *
     * @param phoneNumber 接收通知的电话号码
     * @param notifyType  通知类别，通过该枚举值在配置文件中获取相应的模版ID
     * @param params      通知模版内容里的参数，类似"您的验证码为{1}"中{1}的值
     */
    @Async
    public void notifySmsTemplate(String phoneNumber, NotifyType notifyType, String[] params) {
        if (smsSender == null)
            return;

        int templateId = Integer.parseInt(getTemplateId(notifyType, smsTemplate));

        smsSender.sendWithTemplate(phoneNumber, templateId, params);
    }

    /**
     * 以同步的方式发送短信模版消息通知
     *
     * @param phoneNumber 接收通知的电话号码
     * @param notifyType  通知类别，通过该枚举值在配置文件中获取相应的模版ID
     * @param params      通知模版内容里的参数，类似"您的验证码为{1}"中{1}的值
     * @return
     */
    public SmsResult notifySmsTemplateSync(String phoneNumber, NotifyType notifyType, String[] params) {
        if (smsSender == null)
            return null;

        int templateId = Integer.parseInt(getTemplateId(notifyType, smsTemplate));

        return smsSender.sendWithTemplate(phoneNumber, templateId, params);
    }

    /**
     * 微信模版消息通知,不跳转
     * <p>
     * 该方法会尝试从数据库获取缓存的FormId去发送消息
     *
     * @param touser     接收者openId
     * @param notifyType 通知类别，通过该枚举值在配置文件中获取相应的模版ID
     * @param params     通知模版内容里的参数，类似"您的验证码为{1}"中{1}的值
     */
    @Async
    public void notifyWxTemplate(String touser, NotifyType notifyType, String[] params) {
        if (wxTemplateSender == null)
            return;

        String templateId = getTemplateId(notifyType, wxTemplate);
        //wxTemplateSender.sendWechatMsg(touser, templateId, params);
    }

    /**
     * 微信模版消息通知，带跳转
     * <p>
     * 该方法会尝试从数据库获取缓存的FormId去发送消息
     *
     * @param touser     接收者openId
     * @param notifyType 通知类别，通过该枚举值在配置文件中获取相应的模版ID
     * @param params     通知模版内容里的参数，类似"您的验证码为{1}"中{1}的值
     * @param page       点击消息跳转的页面
     */
    @Async
    public void notifyWxTemplate(String touser, NotifyType notifyType, String[] params, String page) {
        if (wxTemplateSender == null)
            return;

        String templateId = getTemplateId(notifyType, wxTemplate);
        //wxTemplateSender.sendWechatMsg(touser, templateId, params, page);
    }


    private String getTemplateId(NotifyType notifyType, List<Map<String, String>> values) {
        for (Map<String, String> item : values) {
            String notifyTypeStr = notifyType.getType();

            if (item.get("name").equals(notifyTypeStr))
                return item.get("templateId");
        }
        return null;
    }


    public void setSendFrom(String sendFrom) {
        this.sendFrom = sendFrom;
    }


    public void setSendTo(String sendTo) {
        this.sendTo = sendTo;
    }


    public void setSmsSender(SmsSender smsSender) {
        this.smsSender = smsSender;
    }


    public void setSmsTemplate(List<Map<String, String>> smsTemplate) {
        this.smsTemplate = smsTemplate;
    }


    public void setWxTemplateSender(WxTemplateSender wxTemplateSender) {
        this.wxTemplateSender = wxTemplateSender;
    }

    public void setWxTemplate(List<Map<String, String>> wxTemplate) {
        this.wxTemplate = wxTemplate;
    }
}
