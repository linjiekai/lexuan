package com.zhuanbo.core.util;

import org.springframework.validation.BindingResult;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ResponseUtil {

    private static Map<String, Object> map = new HashMap<>();// APP要求返回一个空的JSON

    public static Object ok() {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", 10000);
        obj.put("msg", "成功");
        obj.put("data", map);
        return obj;
    }

    public static Object ok(Object data) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", 10000);
        obj.put("msg", "成功");
        obj.put("data", data);
        return obj;
    }
    

    public static Object ok(String msg, Object data) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", 10000);
        obj.put("msg", msg);
        obj.put("data", data);
        return obj;
    }

    public static Object fail() {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", 10502);
        obj.put("msg", "服务器走神了");
        obj.put("data", map);
        return obj;
    }

    public static Object fail(int code) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", code);
        obj.put("msg", ApplicationYmlUtil.get(code));
        obj.put("data", map);
        return obj;
    }
    
    public static Object fail(int code, String msg) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", code);
        obj.put("msg", msg);
        obj.put("data", map);
        return obj;
    }

    public static Object fail(String code, String msg) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", code);
        obj.put("msg", msg);
        obj.put("data", map);
        return obj;
    }

    public static Object fail(int errno, String msg, Object data) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", errno);
        obj.put("msg", msg);
        obj.put("data", data);
        return obj;
    }

    public static Object result(Integer code, Object data) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", code);
        obj.put("msg", ApplicationYmlUtil.get(code));
        obj.put("data", data == null ? map: data);
        return obj;
    }

    public static Object result(Integer code) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", code);
        obj.put("msg", ApplicationYmlUtil.get(code));
        obj.put("data",map);
        return obj;
    }

    public static Object badArgument(){
        //参数不对
        return result(10401);
    }


    public static Object badArgumentValue(){
        //参数值不对
        return result(10402);
    }

    public static Object badValidate(BindingResult bindingResult){
        return fail(10403, bindingResult.getFieldErrors().stream().map(x -> x.getDefaultMessage()).collect(Collectors.joining(";")));
    }
    public static Object badResult(){
        //数据不存在
        return result(10404);
    }

    public static Object unlogin(){
        //请登录
        return result(10501);
    }

    public static Object serious(){
        //系统内部错误
        return result(10502);
    }

    public static Object unsupport(){
        //业务不支持
        return result(10503);
    }
}

