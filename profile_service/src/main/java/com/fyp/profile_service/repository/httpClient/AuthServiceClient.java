package com.fyp.profile_service.repository.httpClient;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.fyp.profile_service.config.AuthenticationInterceptor;
import com.fyp.profile_service.dto.request.ApiResponse;

@FeignClient(
        name = "auth-service",
        url = "http://localhost:8080",
        configuration = {AuthenticationInterceptor.class})
public interface AuthServiceClient {
    @GetMapping("/identity/users/internal/by-role/{role}")
    ApiResponse<List<String>> getUserIdsByRole(@PathVariable("role") String role);
}
