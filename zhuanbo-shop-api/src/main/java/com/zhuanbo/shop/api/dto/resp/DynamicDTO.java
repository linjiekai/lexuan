package com.zhuanbo.shop.api.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DynamicDTO {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String content;
    private String videoImage;
    private String nickname;
    private String headImgUrl;
    private String likeNumber;
    private Integer liked;// 0:未点赞过、1：点赞过
    private Integer videoWidth;
    private Integer videoHeight;
    private String videoUrl;
    private String goodsName;
    private String goodsUrl;
    private BigDecimal goodsPrice;
    private String commentCount;
    private Long goodsId;
    private String goodsSideName;
    private String shareUrl;
    private String dynamicCreateTime;
}
