package com.zhuanbo.core.util;

public class ContentUtil {

    public final static String STAR = "****";
    /**
     * 名字等转成**格式
     * @param nickName
     * @return
     */
    public static String starName(String nickName) {

        if (nickName == null) {
            return STAR;
        }
        if (nickName.length() < 6) {// 这里变下面也查计算
            return nickName;
        }

        int i = (nickName.length() / 2) - (STAR.length() / 2);
        int i2 = (nickName.length() / 2) + (STAR.length() / 2);

        String n1 = nickName.substring(0, i);
        String n2 = nickName.substring(i2);
        return n1 + STAR + n2;
    }

    // 有效的
    /*public static String starName(String nickName) {
        if (StringUtils.isBlank(nickName)) {
            nickName = "**";
        } else {
            String name = nickName.substring(0, 1);
            String reg = "[\\u4e00-\\u9fa5]+$";
            if (name.matches(reg)) {
                nickName = name+"**";
            } else {
                nickName = nickName.substring(0, nickName.length()> 2 ? 3 : nickName.length()) + "**";
            }
        }
        return nickName;
    }*/
}
