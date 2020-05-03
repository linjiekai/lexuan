package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 业务入口表
 * </p>
 *
 * @author yhh
 * @since 2020-04-20
 */
@Data
@TableName("shop_business_entrance")
public class BusinessEntrance implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
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
     * 逻辑删除
     */
    private Boolean deleted;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
