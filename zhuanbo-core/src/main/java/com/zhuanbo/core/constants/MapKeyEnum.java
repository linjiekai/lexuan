package com.zhuanbo.core.constants;

public enum MapKeyEnum {

    PUSH_NOTIFY("pushNotify"),
    MQ_DATA("mqData"),
    NOTIFY_PUSH_MQ_VO("notifyPushMQVO"),
    PUSH_NOTIFY_ORDER("pushNotifyOrder"),
    PID("pid"),
    PUSH_NOTIFY_LIST("pushNotifyList"),// 消息通知
    ZBMALL_OPENID("zbmall_openid"),
    REGISTER_RESULT("register_result"),// 注册结果
    MQ_PUSH_LIST("mq_push_list"), // mq队列消息
    CODE_PARAMS_VO("codeParamsVO"),// 注册参数
    INVITER("inviter"),//
    INVITER_CODE("inviter_code"),// 邀请码
    USER("user"),
    ACTION("action"),
    DATA("data"),
    UUID("uuid"),
    USER_LOGIN_DATA("userLoginData"),
    ZBMALL_OPENID_BEAN("zbmall_openid_bean"),;

    private String value;

    MapKeyEnum(String value) {
        this.value = value;
    }

    public String value(){
        return this.value;
    }
}
