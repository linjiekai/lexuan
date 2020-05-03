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
 * 用户实名信息表
 * </p>
 *
 * @author rome
 * @since 2019-06-17
 */
@TableName("shop_user_realed")
@Data
public class UserRealed implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 证件类型 1:身份证 2:护照 9:其它
     */
    private Integer cardType;
    /**
     * 证件编号
     */
    private String cardNo;
    /**
     * 证件号简称
     */
    private Integer cardNoAbbr;
    /**
     * 身份证头像面
     */
    private Integer imgFront;
    /**
     * 身份证国徽面
     */
    private Integer imgBack;
    /**
     * 是否默认，0：不是、1：是
     */
    private Integer isDefault;
    /**
     * 删除。0：未删除，1：删除
     */
    private Integer deleted;

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
