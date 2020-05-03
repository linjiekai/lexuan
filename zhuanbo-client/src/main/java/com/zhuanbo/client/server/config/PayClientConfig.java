package com.zhuanbo.client.server.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Configuration
@Slf4j
public class PayClientConfig implements RequestInterceptor {

    @Value("#{${zhuanbo-key}}")
    private Map<String, String> zhuanboKey;
    @Autowired
    private ApplicationContext applicationContext;


    @Override
    public void apply(RequestTemplate template) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
        //定时器会出现null
        if(attributes!=null){
            HttpServletRequest request = attributes.getRequest();
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                //原本的都加进来
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    //这两个重新传
                    if("X-MPMALL-Sign".equalsIgnoreCase(name)||"X-MPMALL-SignVer".equalsIgnoreCase(name)){
                        continue;
                    }
                    String values = request.getHeader(name);
                    template.header(name, values);
                }
            }
        }

        Set<String> strings = zhuanboKey.keySet();
        Iterator<String> iterator = strings.iterator();
        String next = iterator.next();
        template.header("X-MPMALL-SignVer", next);
        template.header("X-MPMALL-APPVer", next);
       //覆盖下
        String s = MDC.get("X-MPMALL-Sign-PAY");
        template.header("X-MPMALL-Sign", s);
    }
}
