package com.zhuanbo.service.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author Administrator
 * @title: FanChartDateVO
 * @description: echart扇形图对象
 * @date 2020/4/28 15:02
 */
@Data
@Builder
public class FanChartDateVO {

    /**
     * name
     */
    private String name;
    /**
     * value
     */
    private Integer value;
}
