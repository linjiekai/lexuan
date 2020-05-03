package com.zhuanbo.service.vo;

import java.io.Serializable;

import com.zhuanbo.core.annotation.ExcelAnnotation;

import lombok.Data;

/**
 * <p>
 * 统计用户分布
 * </p>
 */
@Data
public class StatUserCountVO implements Serializable {

    @ExcelAnnotation(id = 1,name = "日期",width = 5000)
    private String statDate;
    @ExcelAnnotation(id = 2,name = "普通用户",width = 5000)
    private Integer ptLevelCount0;
    @ExcelAnnotation(id = 3,name = "VIP",width = 5000)
    private Integer ptLevelCount1;
    @ExcelAnnotation(id = 4,name = "店长",width = 5000)
    private Integer ptLevelCount2;
    @ExcelAnnotation(id = 5,name = "总监",width = 5000)
    private Integer ptLevelCount3;
    @ExcelAnnotation(id = 6,name = "合伙人",width = 5000)
    private Integer ptLevelCount4;
    @ExcelAnnotation(id = 7,name = "联创",width = 5000)
    private Integer ptLevelCount5;
}
