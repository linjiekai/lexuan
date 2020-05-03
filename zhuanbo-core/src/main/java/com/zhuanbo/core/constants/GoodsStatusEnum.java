package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 商品状态
 * @author Administrator
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum GoodsStatusEnum {

    /*** 0:下架 */
    OFF_SHELVES(0, "下架"),
    /*** 1:上架 */
    ON_SHELVES(1, "上架"),
    /*** 2:缺货 */
    OUT_OF_STOCK(2, "缺货"),
    ;
    private int id;
    private String name;

}
