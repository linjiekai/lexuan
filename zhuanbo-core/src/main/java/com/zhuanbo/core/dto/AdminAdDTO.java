package com.zhuanbo.core.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminAdDTO extends AdminBaseRequestDTO {
    private Integer id;

    /**
     * 平台编号 名品猫:MPMALL;名品玩家:MPWJMALL
     */
    private String platform;

    /**
     * 广告标题
     */
    private String name;

    /**
     * 所广告的商品页面或者活动页面链接地址
     */
    private String link;

    /**
     * 广告宣传图片
     */
    private String url;

    /**
     * 广告位置：1：启动页 2:首页 3.个人中心, 4.优享
     */
    private Integer position;

    /**
     * 广告开始时间
     */

    private LocalDateTime startTime;

    /**
     * 广告结束时间
     */

    private LocalDateTime endTime;
    /**
     * 排序
     */
    private Integer sequenceNumber;

    /**
     * 状态 0:下线 1:待生效 2:生效中
     */
    private Integer status;


    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 创建时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    private Boolean deleted;

    private Integer version;

    private Integer type;// 类型。0:商品、1:活动链接、4品牌页、5：玩家活动跳转连接 6：图片 7:邀请卡（玩家的）

    private String urlIos;// ios的广告宣传图片
    private String urlAndroid;// 安卓的广告宣传图片
}
