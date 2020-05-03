package com.zhuanbo.shop.api.dto.req;

import lombok.Data;

@Data
public class CommentDTO {
    private Long id;
    private Long commentId;
    private Long pid;
    private Long dynamicId;
    private String content;
    private Integer like;
}
