package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Administrator
 * @title: WithdrWeixinOpenEnum
 * @description: 微信提现渠道开关 [0:关闭, 1:开放]
 * @date 2020/4/14 17:35
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum WithdrToWeixinOpenEnum {

    /**
     * 是否开放[微信提现渠道开关]:0:关闭
     */
    OFF(0, "关闭"),
    /**
     * 是否开放[微信提现渠道开关]:1:开放
     */
    ON(1, "开放"),
    ;

    private int id;
    private String name;

}
