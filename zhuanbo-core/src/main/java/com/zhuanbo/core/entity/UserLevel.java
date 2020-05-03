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
 * 会员层级表
 * </p>
 *
 * @author rome
 * @since 2019-06-17
 */
@TableName("shop_user_level")
@Data
public class UserLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 联创
     */
    private Long base;

    /**
     * 合伙人
     */
    private Long partner;

    /**
     * 总监
     */
    private Long director;

    /**
     * 店长
     */
    private Long storeManager;

    /**
     * vip
     */
    private Long vip;

    /**
     * 普通用户
     */
    private Long ordinary;

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
