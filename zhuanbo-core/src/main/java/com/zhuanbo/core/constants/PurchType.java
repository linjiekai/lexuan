package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author: Jiekai Lin
 * @Description(描述): 进货类型
 * @date: 2020/3/23 15:50
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum PurchType {

    /*** 1:购买自用*/
    BUY(1, "购买自用"),
    /*** 2:进货云仓*/
    CLOUD(2, "进货云仓"),
    /*** 3:提货*/
    TAKE(3, "提货"),
    /*** 4:线下发货*/
    OFFLINE(4, "线下发货"),
    /*** 5:礼包提取*/
    GIFT(5, "礼包提取"),
    ONLINE(6, "线上购买")
    ;

    private int id;
    private String name;

    /**
     * 根据id获取类型名称
     * @param id
     * @return
     */
    public static String getNameById(int id) {
        for (PurchType type : values()) {
            if (type.id == id) {
                return type.name;
            }
        }
        return null;
    }

}
