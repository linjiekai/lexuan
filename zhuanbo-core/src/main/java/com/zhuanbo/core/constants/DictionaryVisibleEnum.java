package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Administrator
 * @title: DictionaryVisibleEnum
 * @date 2020/4/1 21:56
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum DictionaryVisibleEnum {

    /*** 0:不可见 */
    INVISIBLE(0, "不可见"),
    /*** 1:可见 */
    VISIBLE(1, "可见"),
    ;
    private int id;
    private String name;
}
