package com.zhuanbo.admin.api.dto.dynamic;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DynamicDTO {
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

    /**
     * 作者id,user_id
     */
    private Long userId;

    private String userName;

    /**
     * 商品id,goods_id
     */
    private Integer goodsId;

    private String goodsName;

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

    private Integer likeNumber;

    private LocalDateTime addTime;

    private LocalDateTime updateTime;
}
