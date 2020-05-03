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
 * 
 * </p>
 *
 * @author rome
 * @since 2019-04-04
 */
@TableName("shop_dynamic")
@Data
public class Dynamic implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 动态内容
     */
    private String content;

    /**
     * 动态内容-视频
     */
    private String videoUrl;

    private String videoId;

    private String videoTranscodeUrl;

    /**
     * 作者id,user_id
     */
    private Long userId;

    /**
     * 商品id,goods_id
     */
    private Integer goodsId;

    /**
     * 开始时间
     */
    private LocalDateTime showTime;

    /**
     * 序号
     */
    private Integer sequenceNumber;

    /**
     * 操作人
     */
    private String operater;

    private Integer deleted;

    /**
     * 点赞数量
     */
    private Integer likeNumber;

    private LocalDateTime addTime;

    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    private Integer videoWidth;

    private Integer videoHeight;
}
