package com.zhuanbo.core.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by rome.chen on 2018/10/23.
 */
public class NumberUtil {
    /**
     * 平均数计算
     *
     * @param num
     * @param groupCount
     * @return
     */
    public static int[] averageANum(double num, int groupCount) {
        DecimalFormat df = new DecimalFormat("#.0");
        String average_str = df.format(num / groupCount);
        String average_str_oneDecimal = average_str.substring(0, average_str.indexOf('.') + 2);
        int[] two_parts = splitADoubleNumByDot(Double.valueOf(average_str_oneDecimal));
        int inteter_part = two_parts[0];
        int decimal_part = two_parts[1];
        if (decimal_part > 5) {
            inteter_part++;
        }

        int[] arr = new int[groupCount];
        for (int i = 0; i < groupCount - 1; i++) {
            arr[i] = inteter_part;
        }
        arr[groupCount - 1] = (int) (num - inteter_part * (groupCount - 1));

        return arr;


    }

    public static int[] splitADoubleNumByDot(double num) {
        String str = Double.toString(num);
        String[] two_parts = str.split("\\.");
        int part1 = Integer.valueOf(two_parts[0]);
        int part2 = Integer.valueOf(two_parts[1]);
        return new int[] {part1, part2};
    }

    public static double BigDecimalAdd(double d1, double d2) {        // 进行加法运算
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.add(b2).doubleValue();
    }

    public static double BigDecimalSub(double d1, double d2) {        // 进行减法运算
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.subtract(b2).doubleValue();
    }

    public static double BigDecimalMul(double d1, double d2) {        // 进行乘法运算
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.multiply(b2).doubleValue();
    }

    public static double BigDecimalDiv(double d1, double d2, int len) {// 进行除法运算
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.divide(b2, len, BigDecimal.
                        ROUND_HALF_UP).doubleValue();
    }
}
