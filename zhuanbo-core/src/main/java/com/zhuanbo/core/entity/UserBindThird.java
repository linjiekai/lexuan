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
 * 用户绑定第三方账号表
 * </p>
 *
 * @author rome
 * @since 2019-03-15
 */
@TableName("shop_user_bind_third")
@Data
public class UserBindThird implements Serializable {

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
     * 用户统一标识 微信unionid
     */
    private String bindId;

    /**
     * 平台编号 名品猫:MPMALL;名品玩家:MPWJMALL
     */
    private String platform;

    /**
     * 头像url地址
     */
    private String imgUrl;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 状态 1:有效 0:无效 用来标识是否绑定
     */
    private Integer bindStatus;

    /**
     * 第三方用户每种登录方式的id,例如微信open_id
     */
    private String openId;

    /**
     * 第三方的token
     */
    private String accessToken;

    /**
     * 会话密钥
     */
    private String sessionKey;

    /**
     * 绑定类型 WEIXIN：微信 ALIPAY:支付宝 YUNXIN:云信 WEIBO：微博 QQ：腾讯qq 
     */
    private String bindType;

    /**
     * 终端渠道：WAP、IOS、ANDROID、WEB、H5、MP、WECHAT
     */
    private String sysCnl;

    /**
     * 绑定日期
     */
    private String regDate;

    /**
     * 绑定时间
     */
    private String regTime;

    /**
     * 乐观锁字段
     */
    private Integer version;

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
