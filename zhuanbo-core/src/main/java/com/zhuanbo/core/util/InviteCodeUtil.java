package com.zhuanbo.core.util;

import java.util.Arrays;
import java.util.List;

/**
 * 邀请码工具类
 */
public class InviteCodeUtil {

    private static final String CUTTER_STR = ",";

    /**
     * 随机生成兑换码
     * 生成规则：6位字母随机生成
     *
     * @return
     */
    public static String createInviteCode() {
        String code = "";
        int size = 6;
        for (int i = 0; i < size; i++) {
            // 生成一个65-90之间的整数
            int inVal = (int) (Math.random() * 26 + 65);
            code += (char) inVal;
        }
        return code;
    }

    /**
     * 随机生成指定数量兑换码
     *
     * @param length 邀请码长度
     * @param num    邀请码个数
     * @return
     */
    public static List<String> createInviteCode(int length, int num) {
        String code = "";
        int size = length * num;
        for (int i = 1; i <= size; i++) {
            // 生成一个65-90之间的整数
            int inVal = (int) (Math.random() * 26 + 65);
            code += (char) inVal;
            if (i % length == 0 && i != size) {
                code += CUTTER_STR;
            }
        }
        String[] split = code.split(CUTTER_STR);
        return Arrays.asList(split);
    }

}
