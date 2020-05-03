package com.zhuanbo.shop.api.dto.req;

import lombok.Data;

@Data
public class UserBuyerDTO extends BaseParamsDTO {

    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 真实姓名
     */
    private String name;

    /**
     * 证件类型 0:身份证 1:护照 9:其它
     */
    private Integer cardType;

    /**
     * 证件编号
     */
    private String cardNo;

    /**
     * 证件号简称 
     */
    private String cardNoAbbr;

    /**
     * 身份证正面
     */
    private String imgFront;

    /**
     * 身份证反面
     */
    private String imgBack;
    
    /**
     * 删除。0：未删除，1：删除
     */
    private Integer deleted;
    
    /**
     * 来源 0：我的 1：提交订单 2：快捷支付 3：提现绑卡
     */
    private Integer realSource;

    /**
     * 下单终端渠道：WAP、IOS、ANDROID、WEB、H5、MP、WECHAT
     */
    private String sysCnl;

    /**
     * 用户客户端IP
     */
    private String clientIp;

}
