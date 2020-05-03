package com.zhuanbo.service.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DynamicVO {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String content;
    private String videoImage;
    private String nickname;
    private String headImgUrl;
    private Integer likeNumber;
    private Integer liked;// 0:未点赞过、1：点赞过
    private Integer videoWidth;
    private Integer videoHeight;
    private String videoUrl;
    private String goodsName;
    private String[] coverImages;
    private BigDecimal goodsPrice;
    private Integer commentCount;
    private Long goodsId;
    private String goodsSideName;
    private LocalDateTime addTime;
    private String videoTranscodeUrl;

}
