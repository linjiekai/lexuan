package com.zhuanbo.admin.api.dto.req;

import lombok.Data;

import java.util.List;

@Data
public class CatDTO {
    private Integer value;
    private String label;
    private List children;


}
