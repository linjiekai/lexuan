package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 搜索历史表
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
@TableName("shop_search_history")
public class SearchHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户表的用户ID
     */
    private Integer userId;

    /**
     * 搜索关键字
     */
    private String keyword;

    /**
     * 搜索来源，如pc、wx、app
     */
    private String from;

    /**
     * 创建时间
     */
    private LocalDateTime addTime;

    /**
     * 逻辑删除
     */
    private Boolean deleted;

    /**
     * 乐观锁字段
     */
    private Integer version;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public LocalDateTime getAddTime() {
        return addTime;
    }

    public void setAddTime(LocalDateTime addTime) {
        this.addTime = addTime;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "SearchHistory{" +
                "id=" + id +
                ", userId=" + userId +
                ", keyword=" + keyword +
                ", from=" + from +
                ", addTime=" + addTime +
                ", deleted=" + deleted +
                ", version=" + version +
                "}";
    }
}
