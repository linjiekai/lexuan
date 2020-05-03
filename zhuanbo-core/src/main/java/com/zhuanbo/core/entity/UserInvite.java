package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户邀请关系表
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
@TableName("shop_user_invite")
@Data
public class UserInvite implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID,与user_id关联
     */
    private Long id;

    /**
     * 邀请人user_id
     */
    private Long pid;

    /**
     * 月份yyyy-MM
     */
    private String inviteMonth;

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
