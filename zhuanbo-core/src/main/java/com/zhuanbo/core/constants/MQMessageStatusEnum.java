package com.zhuanbo.core.constants;

public enum MQMessageStatusEnum {
    STATUS_0(0),// 0:待发送
    STATUS_1(1),// 1：已发送
    STATUS_2(2);// 已消费（客户端）

    private Integer value;
    MQMessageStatusEnum(Integer value){
        this.value = value;
    }
    public Integer value(){
        return this.value;
    }
}
