package com.fyp.authservice.repository.http_client;

import com.fyp.authservice.config.AuthenticationInterceptor;
import com.fyp.authservice.dto.request.ApiResponse;
import com.fyp.authservice.dto.request.ProfileCreationRequest;
import com.fyp.authservice.dto.response.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "profile-service", url = "${spring.app.services.profile}",
        configuration = {AuthenticationInterceptor.class})
public interface ProfileClient {
    //this OpenFeign fetch external data and function as data source for service
    //the same operation with other repository so it should be placed it repository package
        @PostMapping(value = "/internal/users", produces = MediaType.APPLICATION_JSON_VALUE)
        ApiResponse<UserProfileResponse> createProfile
        (@RequestBody  ProfileCreationRequest request);




}
