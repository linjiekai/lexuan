package com.zhuanbo.service.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录参数DTO
 */
@Data
public class AdVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

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
     * 广告位置：1则是首页
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
     * 创建时间
     */
    private LocalDateTime addTime;


    private Integer type;// 类型。0:商品、1:活动链接

    private String urlIos;// ios的广告宣传图片
    private String urlAndroid;// 安卓的广告宣传图片
    /**
     * 状态
     */
    private Integer status;
}
