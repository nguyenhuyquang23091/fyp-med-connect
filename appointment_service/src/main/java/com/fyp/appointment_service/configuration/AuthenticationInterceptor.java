package com.fyp.appointment_service.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class AuthenticationInterceptor implements RequestInterceptor {

    private static final ThreadLocal<String> AUTH_TOKEN_HOLDER = new ThreadLocal<>();

    public static void setAuthToken(String authToken) {
        AUTH_TOKEN_HOLDER.set(authToken);
    }

    public static void clearAuthToken() {
        AUTH_TOKEN_HOLDER.remove();
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String authHeader = null;

        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (servletRequestAttributes != null) {
            authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        } else {
            authHeader = AUTH_TOKEN_HOLDER.get();
        }

        if (StringUtils.hasText(authHeader)) {
            requestTemplate.header("Authorization", authHeader);
        }
    }
}
