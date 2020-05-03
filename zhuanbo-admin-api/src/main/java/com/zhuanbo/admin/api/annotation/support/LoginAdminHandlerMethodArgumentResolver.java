package com.zhuanbo.admin.api.annotation.support;

import com.zhuanbo.core.annotation.LoginAdmin;
import com.zhuanbo.core.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@Slf4j
public class LoginAdminHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String LOGIN_TOKEN_KEY = "Admin-Token";
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(Integer.class)&&parameter.hasParameterAnnotation(LoginAdmin.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container,
                                  NativeWebRequest request, WebDataBinderFactory factory) throws Exception {
        String token = request.getHeader(LOGIN_TOKEN_KEY);
        log.info("Admin-Token:{}", token);
        
        Object value = RedisUtil.get(token);
        if(token == null || token.isEmpty() || value == null){
            return null;
        }
        return value;
    }
}
