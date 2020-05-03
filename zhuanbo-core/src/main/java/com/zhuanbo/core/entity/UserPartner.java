package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 合伙人基础信息表
 * </p>
 *
 * @author rome
 * @since 2019-08-20
 */
@TableName("shop_user_partner")
@Data
public class UserPartner implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID,与user_id关联
     */
    private Long id;

    /**
     * 合伙人等级 [0:普通用户, 1:VIP, 2:店长, 3:总监, 4:合伙人, 5:联创]
     */
    private Integer ptLevel;

    /**
     * 邀请码
     */
    private String inviteCode;

    /**
     * 合伙人编号
     */
    private String ptNo;

    /**
     * 邀请永久有效会员数量
     */
    private Integer ptEffNum;

    /**
     * 授权编号
     */
    private String authNo;
    
    /**
     * 授权书日期
     */
    private String authDate;
    
    /**
     * 我的团队名称
     */
    private String teamName;

    /**
     * 已购买的类型 二进制处理
     */
    private Integer purchasedType;

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
