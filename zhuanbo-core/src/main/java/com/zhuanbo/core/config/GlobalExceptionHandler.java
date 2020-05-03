package com.zhuanbo.core.config;

import com.zhuanbo.core.exception.CheckParamsException;
import com.zhuanbo.core.exception.ShopException;
import com.zhuanbo.core.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.Set;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public Object argumentHandler(MethodArgumentTypeMismatchException e){
        log.error("MethodArgumentTypeMismatchException错误:{}", e);
        return ResponseUtil.badArgumentValue();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public Object argumentHandler(MissingServletRequestParameterException e){
        log.error("MissingServletRequestParameterException错误:{}", e);
        return ResponseUtil.badArgumentValue();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public Object httpMessageNotReadableHandler(HttpMessageNotReadableException e){
        log.error("HttpMessageNotReadableException错误:{}", e);
        return ResponseUtil.badArgumentValue();
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseBody
    public Object handle(ValidationException e) {
        log.error("ValidationException错误:{}", e);
        if(e instanceof ConstraintViolationException){
            ConstraintViolationException exs = (ConstraintViolationException) e;
            Set<ConstraintViolation<?>> violations = exs.getConstraintViolations();
            for (ConstraintViolation<?> item : violations) {
                String message = ((PathImpl)item.getPropertyPath()).getLeafNode().getName() +item.getMessage();
                return ResponseUtil.fail(10402, message);
            }
        }
        return ResponseUtil.badArgumentValue();
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object exceptionHandler(Exception e){
        log.error("Exception错误:{}", e);
        return ResponseUtil.serious();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public Object httpRequestMethodHandler(HttpRequestMethodNotSupportedException e){
        log.error("捕获异常(HttpRequestMethodNotSupportedException.class):{}", e);
        return ResponseUtil.fail("10405", "该方法仅支持 " + e.getSupportedHttpMethods() + " 请求方式");
    }

    @ExceptionHandler(ShopException.class)
    @ResponseBody
    public Object shopExceptionHandler(ShopException e){
        log.error("捕获异常(shopExceptionHandler.class):{}", e);
        if (StringUtils.isBlank(e.getCode())) {
            return ResponseUtil.fail("15102", e.getMsg());
        }
        return ResponseUtil.fail(e.getCode(),e.getMsg());
    }

    @ExceptionHandler(CheckParamsException.class)
    @ResponseBody
    public Object checkParamsExceptionHandler(CheckParamsException e){
        log.error("捕获异常(CheckParamsException.class):{}", e);
        return ResponseUtil.fail("11111", e.getMsg());
    }
}
