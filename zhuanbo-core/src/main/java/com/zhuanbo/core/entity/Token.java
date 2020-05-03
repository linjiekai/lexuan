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
 * token令牌
 * </p>
 *
 * @author rome
 * @since 2019-03-15
 */
@TableName("shop_token")
@Data
public class Token implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * token ID
     */
    private String tokenId;

    /**
     * token日期
     */
    private String tokenDate;

    /**
     * 状态 1:正常 2:失效
     */
    private Integer tokenStatus;

    /**
     * 令牌可使用次数
     */
    private Integer tokenCount;

    /**
     * 令牌已访问次数
     */
    private Integer accessCount;

    /**
     * 令牌失效时间 yyyyMMddHHmmss
     */
    private String expTime;

    /**
     * 绑定方式 WEIXIN:微信 ALIPAY:支付宝 WEIBO:微博 QQ:qq
     */
    private String bindType;

    /**
     * 用户统一标识
     */
    private String bindId;

    /**
     * 用户外部平台开放标识,例如微信open_id
     */
    private String openId;

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
