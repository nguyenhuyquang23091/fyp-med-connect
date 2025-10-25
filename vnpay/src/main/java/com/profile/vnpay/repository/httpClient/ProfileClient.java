package com.profile.vnpay.repository.httpClient;

import com.profile.vnpay.config.AuthenticationInterceptor;
import com.profile.vnpay.dto.response.ApiResponse;
import com.profile.vnpay.dto.response.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "profile-service",
        url = "${microservices.profile-service.url}",
        configuration = {AuthenticationInterceptor.class})
public interface ProfileClient {

    @GetMapping(value = "/profile/users/my-profile")
    ApiResponse<UserProfileResponse> getMyProfile();
}

