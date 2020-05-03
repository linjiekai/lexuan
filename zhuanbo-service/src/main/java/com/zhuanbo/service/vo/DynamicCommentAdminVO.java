package com.zhuanbo.service.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论(管理后台)VO
 */
@Data
public class DynamicCommentAdminVO {

    private Long id;// 评论id
    private String userName;// 评论者用户名
    private String content;// 评论内容
    private String dynamicContent;// 动态源内容
    private Long dynamicId;// 动态id
    private LocalDateTime addTime;// 评论时间
    private LocalDateTime updateTime;// 操作时间
    private Integer deleted;// 删除
    private Integer checked;// 审核
    private String operator;// 操作人
}
