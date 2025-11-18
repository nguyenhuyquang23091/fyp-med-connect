package com.fyp.video_call_service.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class AuthenticationInterceptor implements RequestInterceptor {

    // ThreadLocal to store auth token for async/scheduled threads
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

        // Try to get auth token from current request context (synchronous calls)
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (servletRequestAttributes != null) {
            // Synchronous context - read from HTTP request
            authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        } else {
            // Asynchronous/scheduled context - read from ThreadLocal
            authHeader = AUTH_TOKEN_HOLDER.get();
        }

        // Add Authorization header if present
        if (StringUtils.hasText(authHeader)) {
            requestTemplate.header("Authorization", authHeader);
        }
    }
}
