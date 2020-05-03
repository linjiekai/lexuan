package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @title: OrderConnectTypeEnum
 * @projectName mpmall.api
 * @description: 供应商:订单对接方式
 * @date 2019/10/18 17:55
 */
@Getter
@AllArgsConstructor
public enum OrderConnectTypeEnum {

    /**
     * 订单对接方式: 自动
     */
    AUTO(0, "自动"),
    /**
     * 订单对接方式: 手动
     */
    MANUAL(1, "手动");

    private Integer id;

    private String name;
}
