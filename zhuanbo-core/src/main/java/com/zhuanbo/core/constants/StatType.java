package com.zhuanbo.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum StatType {

    UNSTATISTICS(0, "在途"),
    STATISTICS(1, "累计"),
    ;

    private int id;
    private String name;

    public static StatType parse(int id) {
        for (StatType type : StatType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }
}
