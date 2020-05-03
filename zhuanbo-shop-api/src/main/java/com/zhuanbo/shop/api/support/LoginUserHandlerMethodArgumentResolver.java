package com.zhuanbo.shop.api.support;

import com.zhuanbo.core.annotation.LoginUser;
import com.zhuanbo.core.constants.Constants;
import com.zhuanbo.core.constants.ConstantsEnum;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.RedisUtil;
import com.zhuanbo.service.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;


@Component
@Slf4j
public class LoginUserHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private IUserService iUserService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(Long.class) && parameter.hasParameterAnnotation(LoginUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container,
                                  NativeWebRequest request, WebDataBinderFactory factory) {

        String token = request.getHeader(Constants.LOGIN_TOKEN_KEY);
        log.info("token:{}", token);
        if (token == null || StringUtils.isBlank(token)) {
    		log.info("token对应的用户是空的1");
            return null;
        }
        
        Object obj = RedisUtil.get(token);
        if (obj == null) {
            log.info("token对应的用户是空的2");
            return null;
        }
        log.info("token ---> id:{}", obj);

        Long userId = Long.valueOf(String.valueOf(obj)).longValue();
        if (Constants.COMPANY_USERID.longValue() == userId) {
        	log.error("公司账号，不允许登录");
        	return null;
        }
        //冻结状态，踢出登录
        User byId = iUserService.getById(userId);
        if (byId == null) {
            log.error("空的用户，过滤器");
            return null;
        }
        if(ConstantsEnum.USER_STATUS_2.integerValue().equals(byId.getStatus().intValue())){
            RedisUtil.del(token);
            throw new ShopException(30015);
        }

        return userId;
    }
}
