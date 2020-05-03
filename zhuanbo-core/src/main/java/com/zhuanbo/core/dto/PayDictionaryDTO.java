package com.zhuanbo.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Administrator
 * @title: PayDictionary
 * @date 2020/4/1 11:15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayDictionaryDTO extends BaseDTO implements Serializable {

    private Long id;

    /**
     * 商户号
     */
    private String mercId;

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
     * 是否展示 [0:否, 1:是]
     */
    private Integer visible;

    /**
     * 描述
     */
    private String description;

    /**
     * 操作人id
     */
    private Integer adminId;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 新增时间
     */
    private LocalDateTime addTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


    public PayDictionaryDTO(String category) {
        this.category = category;
    }
}
