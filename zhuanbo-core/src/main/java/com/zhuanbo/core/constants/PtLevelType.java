package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum PtLevelType {

    /*** 0:普通用户 */
    ORDINARY(0, "普通用户"),
    /*** 1:VIP */
    VIP(1, "VIP"),
    /*** 2:店长 */
    STORE_MANAGER(2, "店长"),
    /*** 3:总监 */
    DIRECTOR(3, "总监"),
    /*** 4:合伙人 */
    PARTNER(4, "合伙人"),
    /*** 5:联创 */
    BASE(5, "联创"),
    CC(6, "公司"),
    ;

    private int id;
    private String name;

    public static PtLevelType parse(int id) {
        for (PtLevelType type : PtLevelType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }

    /**
     * 数字转中文
     *
     * @param integer
     * @return
     */
    public static String toName(Integer integer) {
        for (PtLevelType type : PtLevelType.values()) {
            if (integer.equals(type.id)) {
                return type.name;
            }
        }
        return null;
    }

    /**
     * 根据id获取用户等级名称
     *
     * @param id
     * @return
     */
    public static String toName(int id) {
        for (PtLevelType type : PtLevelType.values()) {
            if (id == type.id) {
                return type.name;
            }
        }
        return "";
    }

    /**
     * 等级列表
     * @return
     */
    public static List<Integer>  levelList(){
        List<Integer> li = new ArrayList<Integer>();
        for (PtLevelType type : PtLevelType.values()) {
            li.add(type.id);
        }
        return li;
    }
}
