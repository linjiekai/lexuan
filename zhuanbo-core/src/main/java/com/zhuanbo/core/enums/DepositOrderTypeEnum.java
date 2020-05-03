package com.zhuanbo.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 充值订单类型
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum DepositOrderTypeEnum {

    /*** 1:VIP */
    VIP(1, "VIP"),
    /*** 2:店长 */
    STORE_MANAGER(2, "店长"),
    /*** 3:总监 */
    DIRECTOR(3, "总监"),
    /*** 4:合伙人 */
    PARTNER(4, "合伙人"),
    /*** 5:联创 */
    BASE(5, "联创");
    private int id;
    private String name;
}
