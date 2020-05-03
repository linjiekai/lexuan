package com.zhuanbo.shop.api.support;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.constants.ReqResEnum;
import com.zhuanbo.core.exception.ShopException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 强制更新APP
 */
@Component
@Aspect
@Order(0)
@Slf4j
public class AppAspect {

    private final String V_1_0 = "1.0.";
    private final String V_2_0 = "1.2.";
    private final String LOGIN_URL_0 = "/mobile/auth/login";
    private final String LOGIN_URL_1 = "/mobile/auth/login/wx";
    private final String REGISTER_URL_0 = "/mobile/auth/register";
    private final String REGISTER_URL_1 = "/mobile/auth/wx/bind";

    @Before("execution(public * com.zhuanbo.shop.api.controller..*.*(..)))))")
    public void before(JoinPoint joinPoint) {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String appVer = request.getHeader(ReqResEnum.X_MPMALL_APPVER.String());

        if (StringUtils.isNotBlank(appVer)) {
            String requestURL = request.getRequestURL().toString();
            // v1.0 注册、登录时
            if (appVer.startsWith(V_1_0)) {
                if (requestURL.endsWith(LOGIN_URL_0) || requestURL.endsWith(LOGIN_URL_1)
                        || requestURL.endsWith(REGISTER_URL_0) || requestURL.endsWith(REGISTER_URL_1)) {
                    if (needUpdate(joinPoint)) {
                        throw new ShopException(21000);
                    }
                }
            }
            // v1.2 注册时
            if (appVer.startsWith(V_2_0)) {
                if (requestURL.endsWith(REGISTER_URL_0) || requestURL.endsWith(REGISTER_URL_1)) {
                    if (needUpdate(joinPoint)) {
                        throw new ShopException(21000);
                    }
                }
            }
        }
    }

    /**
     * 如果是旧猫的话，要更新
     * @param joinPoint
     * @return
     */
    private boolean needUpdate(JoinPoint joinPoint) {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = signature.getMethod();
        Object[] objects = joinPoint.getArgs();

        int index = 0;
        Annotation[][] parameterAnnotations = targetMethod.getParameterAnnotations();
        for (Annotation[] parameterAnnotation : parameterAnnotations) {
            for (Annotation annotation : parameterAnnotation) {
                if (annotation instanceof RequestBody) {

                    JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(objects[index]));
                    String platform = jsonObject.getString(ReqResEnum.PLATFORM.String());
                    if (StringUtils.isNotBlank(platform)) {
                        if (ConstantsEnum.PLATFORM_ZBMALL.stringValue().equalsIgnoreCase(platform)) {
                            return true;
                        }
                    } else {// 旧的猫没有platform这字段
                        return true;
                    }
                }
            }
            index++;
        }
        return false;
    }
}
