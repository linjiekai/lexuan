package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 物流公司信息表
 * </p>
 *
 * @author rome
 * @since 2019-04-08
 */
@TableName("shop_ship")
@Data
public class Ship implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 物流公司名称
     */
    private String name;

    /**
     * 物流公司编号 
     */
    private String shipChannel;

    /**
     * 站点
     */
    private String site;

    /**
     * 物流公司官网logo
     */
    private String logoUrl;

    /**
     * 物流公司联系电话
     */
    private String tel;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 修改时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;
}
