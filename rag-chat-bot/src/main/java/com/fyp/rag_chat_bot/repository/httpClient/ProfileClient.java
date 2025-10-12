package com.fyp.rag_chat_bot.repository.httpClient;



import com.fyp.rag_chat_bot.configuration.AuthenticationInterceptor;
import com.fyp.rag_chat_bot.dto.response.ApiResponse;
import com.fyp.rag_chat_bot.dto.response.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@FeignClient(
        name = "profile-service",
        url = "http://localhost:8081",
        //we get our jwt token and decode its information from here
        configuration = {AuthenticationInterceptor.class}
)
public interface ProfileClient {
    @GetMapping(value = "/profile/users/my-profile")
    Optional<ApiResponse<UserProfileResponse>> getMyProfile();

    default Optional<ApiResponse<UserProfileResponse>> getUserProfile(){
        try{
            return getMyProfile();
        } catch (Exception e) {
            return Optional.empty();
        }
    }


}
