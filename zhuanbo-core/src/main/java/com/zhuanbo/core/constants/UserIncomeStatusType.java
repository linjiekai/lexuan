package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 用户收益明细:收益状态
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum UserIncomeStatusType {

    /*** 0:待审核 */
    CHECK(0, "待审核"),
    /*** 1:有效 */
    NORMAL(1, "有效"),
    /*** 2:已扣除 */
    DEDUCT(2, "已扣除 "),
    /*** 3:已过期 */
    EXP(3, "已过期"),
    /*** 4:冻结中 */
    FROZEN(4, "冻结中"),
    /*** 5:冻结返还 */
    FROZENRETURN(5, "冻结返还"),
    /*** 6:冻结扣减 */
    FROZENDEDUCT(6, "冻结扣减"),
    ;
    private int id;
    private String name;

}
