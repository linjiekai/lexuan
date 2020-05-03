package com.zhuanbo.admin.api.annotation.support;


import com.alibaba.fastjson.JSON;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.annotation.LoginUser;
import com.zhuanbo.core.util.AdminidThreadlocal;
import com.zhuanbo.core.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 当请求方法参数中引用LoginAdmin注解，如果LoginAdmin注解对应的参数为空，则要登录
 */
@Aspect
@Component
@Slf4j
public class LoginDealersAspect {

    private ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    private static final String LOGGER_ID = "LOGGER_ID";

    @Around("execution(public * com.zhuanbo.admin.api.dealers.controller..*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        threadLocal.set(System.currentTimeMillis());

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = signature.getMethod();
        Object[] objects = joinPoint.getArgs();// 参数值

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String requestURI = request.getRequestURI();
        Parameter[] parameters = targetMethod.getParameters();

        //admin日志
        String s = "";
        try {
            s = objects.length > 0 ? JSON.toJSONString(objects) : null;
        } catch (Exception e) {
            //有异常先不处理
        }
        log.info("请求URI：{}, 请求参数：{}", requestURI, s);

        if (parameters != null) {
            int i = 0;
            for (Parameter p : parameters) {
                Annotation[] annotations = p.getAnnotations();
                if (annotations != null) {
                    for (Annotation annotation : annotations) {
                        // 有LoginAdmin注解的参数如果没值，要登录
                        if (annotation.annotationType() == LoginUser.class || annotation.annotationType() == LoginAdmin.class) {
                            if (objects[i] == null) {
                                return ResponseUtil.unlogin();
                            } else {
                                AdminidThreadlocal.set(Integer.valueOf(objects[i].toString()));
                            }
                        }
                    }
                }
                i++;
            }
        }
        Object result = joinPoint.proceed();
        log.info("请求处理耗时：{} 毫秒, 返回数据：{}", (System.currentTimeMillis() - threadLocal.get()), JSON.toJSONString(result));
        threadLocal.remove();
        AdminidThreadlocal.remove();
//        MDC.remove(LOGGER_ID);
        return result;
    }
}
