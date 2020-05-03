package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("shop_user_buy_invite_code")
@Data
public class UserBuyInviteCode implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Integer ptLevel;
    private String buyInviteCodeSuffix;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime addTime;
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;
}
