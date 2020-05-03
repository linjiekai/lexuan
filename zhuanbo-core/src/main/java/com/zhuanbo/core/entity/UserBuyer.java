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
 * 用户订购人信息表
 * </p>
 *
 * @author rome
 * @since 2019-09-02
 */
@TableName("shop_user_buyer")
@Data
public class UserBuyer implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 真实姓名
     */
    private String name;

    /**
     * 证件类型 0:身份证 1:护照 9:其它
     */
    private Integer cardType;

    /**
     * 证件编号
     */
    private String cardNo;

    /**
     * 证件号简称 
     */
    private String cardNoAbbr;

    /**
     * 身份证正面
     */
    private String imgFront;

    /**
     * 身份证反面
     */
    private String imgBack;
    
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
