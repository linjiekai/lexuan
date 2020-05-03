package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 直播频道创建表
 * </p>
 *
 * @author rome
 * @since 2019-03-15
 */
@TableName("shop_live_channel")
public class LiveChannel implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 创建人user.id
     */
    private Integer userId;

    /**
     * 状态。0：关闭、1：开启
     */
    private Integer status;

    /**
     * 频道ID，32位字符串
     */
    private String cId;

    /**
     * 创建频道的时间戳
     */
    private String cTime;

    /**
     * 频道名称
     */
    private String name;

    /**
     * 推流地址
     */
    private String pushUrl;

    /**
     * http拉流地址
     */
    private String httpPullUrl;

    /**
     * hls拉流地址
     */
    private String hlsPullUrl;

    /**
     * rtmp拉流地址
     */
    private String rtmpPullUrl;

    /**
     * 观看总人数
     */
    private Long viewerNumber;

    /**
     * 错误信息
     */
    private String msg;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime addTime;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }
    public String getcTime() {
        return cTime;
    }

    public void setcTime(String cTime) {
        this.cTime = cTime;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getPushUrl() {
        return pushUrl;
    }

    public void setPushUrl(String pushUrl) {
        this.pushUrl = pushUrl;
    }
    public String getHttpPullUrl() {
        return httpPullUrl;
    }

    public void setHttpPullUrl(String httpPullUrl) {
        this.httpPullUrl = httpPullUrl;
    }
    public String getHlsPullUrl() {
        return hlsPullUrl;
    }

    public void setHlsPullUrl(String hlsPullUrl) {
        this.hlsPullUrl = hlsPullUrl;
    }
    public String getRtmpPullUrl() {
        return rtmpPullUrl;
    }

    public void setRtmpPullUrl(String rtmpPullUrl) {
        this.rtmpPullUrl = rtmpPullUrl;
    }
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
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

    public Long getViewerNumber() {
        return viewerNumber;
    }

    public void setViewerNumber(Long viewerNumber) {
        this.viewerNumber = viewerNumber;
    }

    @Override
    public String toString() {
        return "ShopLiveChannel{" +
        "id=" + id +
        ", userId=" + userId +
        ", status=" + status +
        ", cId=" + cId +
        ", cTime=" + cTime +
        ", name=" + name +
        ", pushUrl=" + pushUrl +
        ", httpPullUrl=" + httpPullUrl +
        ", hlsPullUrl=" + hlsPullUrl +
        ", rtmpPullUrl=" + rtmpPullUrl +
        ", msg=" + msg +
        ", viewerNumber=" + viewerNumber +
        ", addTime=" + addTime +
        ", updateTime=" + updateTime +
        "}";
    }
}
