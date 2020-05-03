package com.zhuanbo.core.constants;

public enum MQDataTypeEnum {

    PAY_USER_ADD("pay_user_add"),// 支付系统用户添加
    LIVE_USER_ADD("live_user_add"),// 直播系统用户添加
    USER_MODIFY_PROFIT("user_modify_profit");// 分润系统添加

    private String value;
    MQDataTypeEnum(String value){
        this.value = value;
    }
    public String value(){
        return this.value;
    }
}
