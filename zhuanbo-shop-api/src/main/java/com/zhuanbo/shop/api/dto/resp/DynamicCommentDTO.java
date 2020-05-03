package com.zhuanbo.shop.api.dto.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 动态评论详情
 */
@Data
public class DynamicCommentDTO {

    private Long id;
    private Long pId;
    private Long dynamicId;
    private String content;
    private Long fromUid;
    private Long toUid;
    private String fromUserName;
    private String fromUserHeader;
    private String toUserName;
    private LocalDateTime addTime;
    private LocalDateTime updateTime;
}
