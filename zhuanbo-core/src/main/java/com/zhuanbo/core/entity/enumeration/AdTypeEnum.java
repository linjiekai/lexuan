package com.zhuanbo.core.entity.enumeration;

/**
 * @author: Jiekai Lin
 * @Description(描述这个类的作用): 广告类型
 * @date: 2019/8/19 12:57
 */
public enum AdTypeEnum {

    GOODS(0),//商品
    EVENTURL(1),//活动链接
    UNKNOW2(2),//未知活动，占个位置先
    UNKNOW3(3),//未知活动，占个位置先
    BRANDS(4),//品牌
    WJEVENTURL(5),//玩家活动链接
    BANNER(6),//图片
    INVITATIONCARD(7);//玩家邀请卡


    private  Integer type;

    private AdTypeEnum(Integer type){
        this.type = type;
    }
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public static AdTypeEnum getByTpye(int value) {
        for (AdTypeEnum actionEnum : values()) {
            if (actionEnum.getType() == value) {
                return actionEnum;
            }
        }
        return null;
    }
}
