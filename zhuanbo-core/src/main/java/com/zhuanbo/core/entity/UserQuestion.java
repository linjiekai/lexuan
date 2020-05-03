package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户问题反馈
 * </p>
 *
 * @author rome
 * @since 2019-03-15
 */
public class UserQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 日期YYYYMMDD
     */
    private String questionDate;

    /**
     * 日期HHmmss
     */
    private String questionTime;

    /**
     * 状态 W:待处理 S:完成
     */
    private String status;

    /**
     * 图片url, 多张图片之间有逗号隔开
     */
    private String imgUrl;

    /**
     * 问题描述
     */
    private String describe;

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
    public String getQuestionDate() {
        return questionDate;
    }

    public void setQuestionDate(String questionDate) {
        this.questionDate = questionDate;
    }
    public String getQuestionTime() {
        return questionTime;
    }

    public void setQuestionTime(String questionTime) {
        this.questionTime = questionTime;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
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

    @Override
    public String toString() {
        return "UserQuestion{" +
        "id=" + id +
        ", questionDate=" + questionDate +
        ", questionTime=" + questionTime +
        ", status=" + status +
        ", imgUrl=" + imgUrl +
        ", describe=" + describe +
        ", addTime=" + addTime +
        ", updateTime=" + updateTime +
        "}";
    }
}
