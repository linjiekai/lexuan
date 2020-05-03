package com.zhuanbo.core.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DictionaryDTO extends AdminBaseRequestDTO {

    private Long id;

    /**
     * 类别
     */
    private String category;

    /**
     * 名称
     */
    private String name;

    /**
     * 字符串值
     */
    private String strVal;

    /**
     * 长/整形值
     */
    private Long longVal;

    /**
     * 描述
     */
    private String description;

    /**
     * 添加时间
     */
    private LocalDateTime addTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
