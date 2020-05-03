package com.zhuanbo.core.constants;

public enum OrderMQTypeEnum {

    ORDER_TYPE_F_1(-1),// 商品99购买
    ORDER_TYPE_2(2),// 赚播店长999
    ORDER_TYPE_3(3),// 赚播总监9999
    ORDER_TYPE_4(4),// 赚播合伙人30000
    ORDER_TYPE_5(5);// 赚播联创89100

    private Integer value;
    OrderMQTypeEnum(Integer value){
        this.value = value;
    }
    public Integer value(){
        return this.value;
    }
}
