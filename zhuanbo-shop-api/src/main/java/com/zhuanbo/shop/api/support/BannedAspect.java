package com.zhuanbo.shop.api.support;

import com.zhuanbo.core.annotation.LoginUser;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.service.service.IUserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * 用户封禁行为限制
 */
@Component
@Aspect
@Order(1)
public class BannedAspect {

    @Autowired
    private IUserService iUserService;

    @Before("execution(public * com.zhuanbo.shop.api.controller..*.*(..)))))")
    public void before(JoinPoint joinPoint) {

        MethodSignature ms = (MethodSignature) joinPoint.getSignature();
        Method method = ms.getMethod();

        Object[] params = joinPoint.getArgs();

        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        int i = 0;
        for (Annotation[] parameterAnnotation : parameterAnnotations) {
            for (Annotation annotation : parameterAnnotation) {
                if (annotation instanceof LoginUser) {
                    Object parameterValue = params[i];
                    Optional.ofNullable(parameterValue).ifPresent(x -> {
                        User user = iUserService.getById(Long.valueOf(x.toString()));
                        if (ConstantsEnum.USER_STATUS_2.integerValue().equals(user.getStatus())) {
                            throw new ShopException(10033);
                        }
                    });
                }
            }
            i++;
        }
    }
}
