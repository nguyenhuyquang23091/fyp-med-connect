package com.profile.profile_service.config;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class AuthenticationInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        // use to get specific tokenId
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var header = servletRequestAttributes.getRequest().getHeader("Authorization");
        if (StringUtils.hasText(header)) {
            requestTemplate.header("Authorization", header);
        }
    }
}
