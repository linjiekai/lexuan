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
 * APP版本管理表
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
@TableName("shop_app_version")
@Data
public class AppVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 平台编号 名品猫:MPMALL;名品玩家:MPWJMALL
     */
    private String platform;

    /**
     * 终端渠道：IOS、ANDROID
     */
    private String sysCnl;

    /**
     * 版本日期 yyyy-MM-dd
     */
    private String versionDate;

    /**
     * 版本时间 HH:mm:ss
     */
    private String versionTime;

    /**
     * 当前版本号
     */
    private String version;

    /**
     * 生效版本号
     */
    private String effVersion;

    /**
     * 状态 0：待生效 1：生效中 2：已失效
     */
    private Integer status;

    /**
     * 状态 0：提示更新 1：强制更新
     */
    private Integer forced;

    /**
     * 跳转url地址
     */
    private String redirectUrl;

    /**
     * 下载url地址
     */
    private String downloadUrl;

    /**
     * 内容
     */
    private String content;

    /**
     * 生效时间
     */
    private LocalDateTime effTime;

    /**
     * 是否删除。0：否，1：是
     */
    private Integer deleted;

    /**
     * 操作人员
     */
    private String operator;

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
