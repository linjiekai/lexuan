package com.zhuanbo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author rome
 * @since 2019-03-14
 */
public class IndexTopicGoods implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 首页主题id
     */
    private Integer indexTopicId;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 添加时间
     */
    private LocalDateTime addTime;

    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Integer getIndexTopicId() {
        return indexTopicId;
    }

    public void setIndexTopicId(Integer indexTopicId) {
        this.indexTopicId = indexTopicId;
    }
    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
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
        return "IndexTopicGoods{" +
        "id=" + id +
        ", indexTopicId=" + indexTopicId +
        ", goodsId=" + goodsId +
        ", addTime=" + addTime +
        ", updateTime=" + updateTime +
        "}";
    }
}
