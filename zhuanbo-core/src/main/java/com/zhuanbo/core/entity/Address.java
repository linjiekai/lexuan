package com.zhuanbo.core.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 收货地址表
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@TableName("shop_address")
@Data
public class Address implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 收货人名称
     */
    private String name;

    /**
     * 用户表的用户ID
     */
    private Long userId;

    /**
     * 行政区域表的省ID
     */
    private Integer provinceId;

    /**
     * 行政区域表的市ID
     */
    private Integer cityId;

    /**
     * 行政区域表的区县ID
     */
    private Integer areaId;

    /**
     * 乡镇Id
     */
    private Integer countryId;

    /**
     * 具体收货地址
     */
    private String address;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 是否默认地址
     */
    private Integer isDefault;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime addTime;

    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    private Integer deleted;


    // 辅助字段 start
    @TableField(exist = false)
    private String provinceName;
    @TableField(exist = false)
    private String cityName;
    @TableField(exist = false)
    private String areaName;
    // 辅助字段 end
}

