package com.zhuanbo.core.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BusinessEntranceDTO extends AdminBaseRequestDTO {
    private Integer id;

    /**
     * 排序
     */
    private Integer sequenceNumber;

    /**
     * 跳转链接
     */
        private String link;

    /**
     * 图片
     */
    private String url;

    /**
     * 状态：0:关闭 1:开放
     */
    private Integer status;

    /**
     * 操作者
     */
    private String operator;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
