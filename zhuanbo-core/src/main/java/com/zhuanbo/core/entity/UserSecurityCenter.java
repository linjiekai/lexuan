package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户安全中心表
 * </p>
 *
 * @author rome
 * @since 2019-03-15
 */
@TableName("shop_user_security_center")
public class UserSecurityCenter implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键用户ID, 与user表user_id对应
     */
    private Long id;

    /**
     * 绑定用户登录名称 0:未绑定 1:已绑定
     */
    private Integer bindUserName;

    /**
     * 绑定登录密码 0:未绑定 1:已绑定
     */
    private Integer bindPassword;

    /**
     * 绑定手机号 0:未绑定 1:已绑定
     */
    private Integer bindMobile;

    /**
     * 绑定邮箱 0:未绑定 1:已绑定
     */
    private Integer bindEmail;

    /**
     * 绑定证件号 0:未绑定 1:已绑定
     */
    private Integer bindCardNo;

    /**
     * 绑定问题 0:未绑定 1:已绑定
     */
    private Integer bindQuestion;

    /**
     * 绑定微信 0:未绑定 1:已绑定
     */
    private Integer bindWeixin;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Integer getBindUserName() {
        return bindUserName;
    }

    public void setBindUserName(Integer bindUserName) {
        this.bindUserName = bindUserName;
    }
    public Integer getBindPassword() {
        return bindPassword;
    }

    public void setBindPassword(Integer bindPassword) {
        this.bindPassword = bindPassword;
    }
    public Integer getBindMobile() {
        return bindMobile;
    }

    public void setBindMobile(Integer bindMobile) {
        this.bindMobile = bindMobile;
    }
    public Integer getBindEmail() {
        return bindEmail;
    }

    public void setBindEmail(Integer bindEmail) {
        this.bindEmail = bindEmail;
    }
    public Integer getBindCardNo() {
        return bindCardNo;
    }

    public void setBindCardNo(Integer bindCardNo) {
        this.bindCardNo = bindCardNo;
    }
    public Integer getBindQuestion() {
        return bindQuestion;
    }

    public void setBindQuestion(Integer bindQuestion) {
        this.bindQuestion = bindQuestion;
    }
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
    public LocalDateTime getAddTime() {
        return addTime;
    }

    public void setAddTime(LocalDateTime addTime) {
        this.addTime = addTime;
    }
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getBindWeixin() {
        return bindWeixin;
    }

    public void setBindWeixin(Integer bindWeixin) {
        this.bindWeixin = bindWeixin;
    }

    @Override
    public String toString() {
        return "UserSecurityCenter{" +
        "id=" + id +
        ", bindUserName=" + bindUserName +
        ", bindPassword=" + bindPassword +
        ", bindMobile=" + bindMobile +
        ", bindEmail=" + bindEmail +
        ", bindCardNo=" + bindCardNo +
        ", bindQuestion=" + bindQuestion +
        ", bindWeixin=" + bindWeixin +
        ", version=" + version +
        ", addTime=" + addTime +
        ", updateTime=" + updateTime +
        "}";
    }
}
