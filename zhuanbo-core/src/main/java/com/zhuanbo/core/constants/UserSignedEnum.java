package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 用户是否进行代理协议签名
 *
 * @date 2019/11/6 16:02
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum UserSignedEnum {

    /**
     * 0:未签名
     */
    NO(0, "未签名"),
    /**
     * 1:已签名
     */
    YES(1, "已签名"),
    ;

    private int id;
    private String name;
}
