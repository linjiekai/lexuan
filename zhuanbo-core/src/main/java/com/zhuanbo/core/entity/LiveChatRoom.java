package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author rome
 * @since 2019-03-12
 */
@TableName("shop_live_chat_room")
public class LiveChatRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 云信聊天室的room_id
     */
    private Long roomId;

    /**
     * 聊天室属主的账号accid
     */
    private Integer userId;

    /**
     * 聊天室名称，长度限制128个字符
     */
    private String name;

    /**
     * 直播地址，长度限制1024个字符
     */
    private String broadcastUrl;

    /**
     * 频道c_id,对就shop_live_channel.c_id
     */
    private String cId;

    /**
     * 状态。0：关闭、1：开启
     */
    private Integer status;

    private LocalDateTime addTime;

    private LocalDateTime udpateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getBroadcastUrl() {
        return broadcastUrl;
    }

    public void setBroadcastUrl(String broadcastUrl) {
        this.broadcastUrl = broadcastUrl;
    }

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
    public LocalDateTime getAddTime() {
        return addTime;
    }

    public void setAddTime(LocalDateTime addTime) {
        this.addTime = addTime;
    }
    public LocalDateTime getUdpateTime() {
        return udpateTime;
    }

    public void setUdpateTime(LocalDateTime udpateTime) {
        this.udpateTime = udpateTime;
    }

    @Override
    public String toString() {
        return "ShopLiveChatRoom{" +
        "id=" + id +
        ", roomId=" + roomId +
        ", userId=" + userId +
        ", name=" + name +
        ", broadcastUrl=" + broadcastUrl +
        ", cId=" + cId +
        ", status=" + status +
        ", addTime=" + addTime +
        ", udpateTime=" + udpateTime +
        "}";
    }
}
