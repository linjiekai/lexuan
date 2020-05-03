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
 *
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@TableName("shop_user_formid")
@Data
public class UserFormid implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 缓存的FormId
     */
    @TableField("formId")
    private String formId;

    /**
     * 是FormId还是prepayId
     */
    private Boolean isprepay;

    /**
     * 可用次数，fromId为1，prepay为3，用1次减1
     */
    @TableField("useAmount")
    private Integer useAmount;

    /**
     * 过期时间，腾讯规定为7天
     */
    private LocalDateTime expireTime;

    /**
     * 微信登录openid
     */
    @TableField("openId")
    private String openId;

    /**
     * 乐观锁字段
     */
    private Integer version;


}
