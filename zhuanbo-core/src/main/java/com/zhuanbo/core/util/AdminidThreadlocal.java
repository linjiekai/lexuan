package com.zhuanbo.core.util;

public class AdminidThreadlocal {

    private static ThreadLocal<Integer> adminidThreadlocal = new ThreadLocal<>();// 全局线程adminid
    public static void set(Integer integer) {
        adminidThreadlocal.set(integer);
    }

    public static Integer get() {
        return adminidThreadlocal.get();
    }

    public static void remove() {
        adminidThreadlocal.remove();
    }
}
