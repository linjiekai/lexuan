package com.zhuanbo.core.dto;

import lombok.Data;

@Data
public class MobileBaseParamsDTO {

    private static final long serialVersionUID = 1L;
    private Integer page = 1;// 分页
    private Integer limit = Integer.MAX_VALUE;// 每页数量,默认10页
    private String sort = "id";// 排序字段
    private Long id;
    private Integer dynamicId;
    private String mercId = "888000000000004";// 商户号
    private String platform = "MPMALL";// 平台编号
    private Long userId = 0L;// 用户ID
}
