package com.zhuanbo.service.vo;

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
public class TopicsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    /**
     * 1:普通列表 2:橱窗列表 3:横铺列表
     */
    private Integer goodsId;


    private Integer type;

    /**
     * 封面图片
     */
    private String coverImage;

    /**
     * 主标题
     */
    private String mainTitle;

    /**
     * 副标题
     */
    private String sideTitle;


    /**
     * 序号
     */
    private Integer sequenceNumber;

    private Boolean enable;

    private LocalDateTime addTime;

    private LocalDateTime updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }
    public String getMainTitle() {
        return mainTitle;
    }

    public void setMainTitle(String mainTitle) {
        this.mainTitle = mainTitle;
    }
    public String getSideTitle() {
        return sideTitle;
    }

    public void setSideTitle(String sideTitle) {
        this.sideTitle = sideTitle;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
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

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    @Override
    public String toString() {
        return "IndexTopic{" +
                "id=" + id +
                ", type=" + type +
                ", coverImage='" + coverImage + '\'' +
                ", mainTitle='" + mainTitle + '\'' +
                ", sideTitle='" + sideTitle + '\'' +
                ", sequenceNumber=" + sequenceNumber +
                ", enable=" + enable +
                ", addTime=" + addTime +
                ", updateTime=" + updateTime +
                '}';
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }
}
