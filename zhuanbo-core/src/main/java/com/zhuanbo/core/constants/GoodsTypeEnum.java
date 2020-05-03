package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 商品类型
 *
 * @author Administrator
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum GoodsTypeEnum {

    /*** -1:全部 */
    ALL(-1, "全部"),
    /*** 0:商品 */
    GOODS(0, "商品"),
    /*** 1:赠品 */
    GIFT(1, "赠品"),
    ;
    private int id;
    private String name;
}
