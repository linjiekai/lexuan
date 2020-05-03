package com.zhuanbo.core.util;


import org.hashids.Hashids;

/**
 * hashid工具类，id到唯一hashid
 */
public class HashIdUtil {

    private HashIdUtil(){}

    private static class HashIdUtilSingle{
        // 长度16位的唯一hashid
        private static final Hashids hashids16 =
                new Hashids("XFHLNICE", 16 , "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
    }

    public static String hashId16(Long id){
        return HashIdUtilSingle.hashids16.encode(id);
    }

    public static void main(String[] args) throws InterruptedException {

        System.out.println(hashId16(253L));
        System.out.println(hashId16(397L));
    }
}