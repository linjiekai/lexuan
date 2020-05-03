package com.zhuanbo.core.dto;

import lombok.Data;

/**
 * @Description(描述): 管理后台分页入参的顶层DTO
 * @auther: Jack Lin
 * @date: 2019/7/8 15:19
 */
@Data
public class AdminBaseRequestDTO {
    private Integer page = 1;
    private Integer limit = 10;

    /**
     * 操作人id
     */
    private Integer operatorId;

    /**
     * 操作人
     */
    private String operator;
}
