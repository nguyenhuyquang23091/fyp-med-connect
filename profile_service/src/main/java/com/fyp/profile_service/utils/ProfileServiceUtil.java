package com.fyp.profile_service.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fyp.profile_service.exceptions.AppException;
import com.fyp.profile_service.exceptions.ErrorCode;

public final class ProfileServiceUtil {
    private ProfileServiceUtil() {}

    // define publish CDC event in here
    public static String getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    public static String getAuthorizationToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String authHeader = attributes.getRequest().getHeader("Authorization");

        if (authHeader == null || authHeader.isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return authHeader;
    }
}
