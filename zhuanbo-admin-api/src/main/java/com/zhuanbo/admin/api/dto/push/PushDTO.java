package com.zhuanbo.admin.api.dto.push;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PushDTO {


    private Long id;

    /**
     * 主标题
     */
    private String title;

    /**
     * 副标题
     */
    private String subtitle;

    /**
     * 跳转位置,商品id或动态id
     */
    private Long targetId;

    /**
     * 跳转位置：活动url
     */
    private String targetUrl;

    /**
     * 跳转位置：标题或名称
     */
    private String targetContent;

    /**
     * 推送范围,用户ids
     */
    private String targetUserIds;

    /**
     * 类型。0:商品、1:活动链接'、2：动态
     */
    private Integer type;

    /**
     * 推送时间
     */
    private LocalDateTime pushTime;

    /**
     * 状态 0：待推送、1已推送、2下线
     */
    private Integer status;

    /**
     * 操作人
     */
    private String operator;

    private String platform = "ZBMALL";

    private LocalDateTime addTime;

    private LocalDateTime updateTime;

}
