package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @title: WithdrOrderStatusEnum
 * @projectName mpmall.api
 * @description: 提现订单:订单状态
 * @date 2019/10/22 10:59
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum WithdrOrderStatusEnum {

    /**
     * C:创建订单
     */
    CREATE("C", "创建订单"),
    /**
     * W:待提现/审核中
     */
    WAIT("W", "待提现"),
    /**
     * S:提现成功
     */
    SUCCESS("S", "提现成功"),
    /**
     * F:提现失败
     */
    FAIL("F", "提现失败"),
    /**
     * A:待审核
     */
    AUDIT_WAIT("A", "待审核"),
    /**
     * R:审核拒绝
     */
    REJECT("R", "审核拒绝"),
    /**
     * E:提现异常
     */
    ERROR("E", "提现异常"),
    /**
     * TS:第三方机构处理成功
     */
    THIRD_SUCCESS("TS", "第三方机构处理成功"),
    ;

    private String id;
    private String name;

    /**
     * 根据id获取名称
     * @param id
     * @return
     */
    public static String getNameById(String id) {
        for (WithdrOrderStatusEnum status : WithdrOrderStatusEnum.values()) {
            if (status.id.equals(id)) {
                return status.name;
            }
        }
        return null;
    }

}
