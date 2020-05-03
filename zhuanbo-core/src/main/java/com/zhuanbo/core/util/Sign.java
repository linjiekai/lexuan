package com.zhuanbo.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

@Slf4j
public class Sign {

    //生成待签名串
    public static String getPlain(Map<String, Object> requestMap, String[] keys) throws Exception {
        StringBuffer plain = new StringBuffer("");

        //数组按KEY排序
        Arrays.sort(keys);

        Object value = null;
        //循环拼plain字符key1=value1&key2=value2&key3=value3…
        for (String key : keys) {
            value = requestMap.get(key.trim());
            //值为空的字段不参与签名
            if (null != value && !"".equals(value.toString().trim()) && !"sign".equals(key.toString().trim())
                    && !(value instanceof List) && !(value instanceof Map)) {
                plain.append(key + "=" + value.toString().trim() + "&");
            }
        }
        //去掉最后一个&符号
        if (plain.length() > 0 && plain.lastIndexOf("&") >= 0) {
            plain.deleteCharAt(plain.lastIndexOf("&"));
        }
        return plain.toString();
    }

    //生成待签名串
    public static String getPlain(Map<String, Object> requestMap) throws Exception {
        return getPlain(requestMap, false);
    }

    /**
     * 生成待签名串
     *
     * @param requestMap
     * @param composite  false去掉复合类型  true保留复合类型
     * @return
     * @throws Exception
     */
    public static String getPlain(Map<String, Object> requestMap, boolean composite) throws Exception {
        StringBuffer plain = new StringBuffer("");

        List<String> keys = new ArrayList<String>(requestMap.keySet());
        Collections.sort(keys);

        Object value = null;

        //循环拼plain字符key1=value1&key2=value2&key3=value3…
        for (String key : keys) {
            value = requestMap.get(key.trim());
            //值为空的字段不参与签名
            if (null != value && !"".equals(value.toString().trim()) && !"sign".equals(key.toString().trim())
                    && (composite || (!(value instanceof List) && !(value instanceof Map)))
            ) {
                plain.append(key + "=" + value.toString().trim() + "&");
            }
        }

        //去掉最后一个&符号
        if (plain.length() > 0 && plain.lastIndexOf("&") >= 0) {
            plain.deleteCharAt(plain.lastIndexOf("&"));
        }
        return plain.toString();
    }

    public static String getPlainURLEncoder(Map<String, Object> requestMap, String charset) throws IOException {
        StringBuffer plain = new StringBuffer("");

        List<String> keys = new ArrayList<String>(requestMap.keySet());
        Collections.sort(keys);

        Object value = null;

        //循环拼plain字符key1=value1&key2=value2&key3=value3…
        for (String key : keys) {
            value = requestMap.get(key.trim());
            //值为空的字段不参与签名
            if (null != value && !"".equals(value.toString().trim()) && !"sign".equals(key.toString().trim())
                    && !(value instanceof List) && !(value instanceof Map)
            ) {
                plain.append(key + "=" + URLEncoder.encode(value.toString().trim(), charset) + "&");
            }
        }

        //去掉最后一个&符号
        if (plain.length() > 0 && plain.lastIndexOf("&") >= 0) {
            plain.deleteCharAt(plain.lastIndexOf("&"));
        }
        return plain.toString();

    }

    //签名
    public static String sign(Map<String, Object> requestMap, String key) throws Exception {
        String plain = Sign.getPlain(requestMap);
        plain += "&key=" + key;
        log.info("plain[{}]", plain);
        String sign = Sign.sign(plain);
        log.info("sign[{}]", sign);
        return sign;
    }

    //签名
    public static String signToHex(String plain) throws Exception {

        byte[] data;
        data = MD5Sign.encode(plain.getBytes("UTF-8"));
        return HexStr.bytesToHexString(data);
    }

    //签名
    public static String sign(String plain) throws Exception {
        return Base64.encodeBase64String(signToHex(plain).getBytes());
    }

    //验证签名
    public static boolean verify(String plain, String sign) throws Exception {
        if (sign.equalsIgnoreCase(sign(plain))) {
            return true;
        }

        return false;
    }

    //验证签名
    public static boolean verifyToHex(String plain, String sign) throws Exception {
        if (sign.equalsIgnoreCase(signToHex(plain))) {
            return true;
        }

        return false;
    }

    public static void main(String[] args) throws Exception {
    	
    	String plain = "X-MPMALL-SignVer=v1&busiType=02&clientIp=0.0.0.0&mercId=888000000000004&methodType=DirectPrePay&mobile=13119656021&notifyUrl=https://test-zb-api.zhuanbo.gdxfhl.com/shop/mobile/pay/notify&orderDate=2020-04-26&orderNo=2020042600231160&orderTime=18:09:08&period=1&periodUnit=02&platform=ZBMALL&price=36.00&requestId=1587895748477&sysCnl=WX-APPLET&tradeCode=02&userId=11182&key=12345678";
    	
    	System.out.println(sign(plain));
    	
//鉴权失败:Get Sign:YjQ4MjkzMzhjOTY2NTU2NmJlNTNkNGM3NGVmZTNjYzM=, Server Sign:Y2IyNmViZjc5MDAwYzQxZGI1ODgwMjQ3ZGRhYmVjMDA=
    		
    		
    	System.out.println();
    	
    	
        try {
        	/**
            String before = "asdf";
            byte[] plainText = before.getBytes("UTF8");
//形成RSA公钥对 
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            KeyPair key = keyGen.generateKeyPair();
//使用私钥签名********************************************************** 
            Signature sig = Signature.getInstance("SHA1WithRSA");
            sig.initSign(key.getPrivate());
//sig对象得到私钥 
//签名对象得到原始数据 
            sig.update(plainText);
//sig对象得到原始数据(现实中用的是原始数据的摘要，摘要的是单向的，即摘要算法后无法解密) 
            byte[] signature = sig.sign();
//sig对象用私钥对原始数据进行签名，签名后得到签名signature 
            System.out.println(sig.getProvider().getInfo());
            String after1 = new String(signature, "UTF8");
            System.out.println("用私钥签名后:" + after1);
//使用公钥验证 
            sig.initVerify(key.getPublic());
//sig对象得到公钥 
//签名对象得到原始信息 
            sig.update(plainText);
*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
