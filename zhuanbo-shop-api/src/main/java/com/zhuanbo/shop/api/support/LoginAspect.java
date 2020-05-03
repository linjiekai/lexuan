package com.zhuanbo.shop.api.support;


import com.alibaba.fastjson.JSON;
import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.annotation.LoginUser;
import com.zhuanbo.core.annotation.ResponseLog;
import com.zhuanbo.core.util.LogUtil;
import com.zhuanbo.core.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.UUID;

/**
 * 当请求方法参数中引用LoginAdmin注解，如果LoginAdmin注解对应的参数为空，则要登录
 */
@Aspect
@Component
@Slf4j
public class LoginAspect {

    private static final String LOGGER_ID = "LOGGER_ID";
    private ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    @Pointcut("execution(public * com.zhuanbo.shop.api.mq..*.*(..))")
    public void mq() {
    }

    @Pointcut("execution(public * com.zhuanbo.shop.api.scheduled..*.*(..))")
    public void scheduled() {
    }

    @Around("execution(public * com.zhuanbo.shop.api.controller..*.*(..)) || @annotation(com.zhuanbo.core.annotation.ResponseLog)))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        threadLocal.set(System.currentTimeMillis());

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = signature.getMethod();
        Object[] objects = joinPoint.getArgs();// 参数值

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String requestURI = request.getRequestURI();
        Parameter[] parameters = targetMethod.getParameters();
        String s = "";
        try {
            s = objects.length > 0 ? JSON.toJSONString(objects) : null;
        } catch (Exception e) {
            //有异常先不处理
        }
        if (joinPoint.getTarget().getClass().getAnnotation(ResponseLog.class) != null || targetMethod.getAnnotation(ResponseLog.class) != null) {
            log.info("请求URI：{}, 请求参数：{}", requestURI, s);
        } else {
            LogUtil.COLD_FILE.info("请求URI：{}, 请求参数：{}", requestURI, s);
        }

        if (parameters != null) {
            int i = 0;
            for (Parameter p : parameters) {
                Annotation[] annotations = p.getAnnotations();
                if (annotations != null) {
                    for (Annotation annotation : annotations) {
                        if (annotation.annotationType() == LoginUser.class || annotation.annotationType() == LoginAdmin.class) {// 有LoginUser注解的参数如果没值，要登录
                            if (objects[i] == null) {
                                return ResponseUtil.unlogin();
                            }
                        }
                    }
                }
                i++;
            }
        }
        Object result = joinPoint.proceed();
        if (joinPoint.getTarget().getClass().getAnnotation(ResponseLog.class) != null || targetMethod.getAnnotation(ResponseLog.class) != null) {
            log.info("请求处理耗时：{} 毫秒, 返回数据：{}", (System.currentTimeMillis() - threadLocal.get()), JSON.toJSONString(result));
        } else {
            if (!(requestURI.indexOf("/list") > 0
                    || requestURI.indexOf("/index") > 0)
            ) {

                LogUtil.COLD_FILE.info("请求处理耗时：{} 毫秒, 返回数据：{}", (System.currentTimeMillis() - threadLocal.get()), JSON.toJSONString(result));
            }
        }
        threadLocal.remove();
        MDC.clear();
        return result;
    }


    @Before("mq() || scheduled()")
    public void before(JoinPoint joinPoint) {
        // 日志
        if (StringUtils.isEmpty(MDC.get(LOGGER_ID))) {
            MDC.put(LOGGER_ID, UUID.randomUUID().toString());
        }
    }

    @After("mq() || scheduled()")
    public void after(JoinPoint joinPoint) {
        MDC.remove(LOGGER_ID);
    }
}
