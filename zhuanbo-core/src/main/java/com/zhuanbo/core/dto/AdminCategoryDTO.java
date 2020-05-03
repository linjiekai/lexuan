package com.zhuanbo.core.dto;

import lombok.Data;

@Data
public class AdminCategoryDTO extends AdminBaseRequestDTO {
    public Long id;
    public String name;
    //层级
    private Integer level;
    //父类目id
    private Long pid;
    /**
     * 排序
     */
    private Integer indexs;

    /**
     * 状态 1:上线 2：下线
     */
    private Integer status;

    /**
     * 操作人ID
     */
    private Integer adminId;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 逻辑删除
     */
    private Integer deleted;
    /**
     * 父类目名称
     */
    private String pidName;
}
