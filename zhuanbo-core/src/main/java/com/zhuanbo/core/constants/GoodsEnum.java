package com.zhuanbo.core.constants;

public enum GoodsEnum {
    TRACE_TYPE_0(0),
    TRACE_TYPE_1(1),
    TRACE_TYPE_2(2),
    TRACE_TYPE_3(3),
    BUYER_PARTNER_0(0),
    BUYER_PARTNER_1(1),
    /**普通商品*/
    GOODS_TYPE_0(0),
    /**会员商品*/
    GOODS_TYPE_1(1),
    /**永久会员*/
    BUYER_EFF_1(1),
    /**非永久会员*/
    BUYER_EFF_0(0),
    ;
    private Object value;

    GoodsEnum(Object value){
        this.value = value;
    }

    public Integer Integer(){
        return Integer.valueOf(value.toString());
    }

    public String String(){
        return value.toString();
    }
    
}
