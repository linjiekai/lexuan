package com.zhuanbo.service.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 动态评论详情
 */
@Data
public class DynamicCommentVO {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long pid;
    private Long dynamicId;
    private Integer fromUid;
    private String content;
    private String fromUserName;
    private String headImgUrl;
    private String toUserName;
    private String dynamicTime;
    private LocalDateTime addTime;
    private Boolean isOwn;
}
