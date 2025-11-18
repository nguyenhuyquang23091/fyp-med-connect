package com.fyp.video_call_service.repository.httpCLient;


import com.fyp.video_call_service.configuration.AuthenticationInterceptor;
import com.fyp.video_call_service.dto.request.ApiResponse;
import com.fyp.video_call_service.dto.request.VideoCallNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "notification-service",
        url = "http://localhost:8085/notifications",
        configuration = AuthenticationInterceptor.class)
public interface NotificationFeignClient {
    @PostMapping(
            value = "/send-video-call-notification",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<String> sendVideoCallNotification(@RequestBody VideoCallNotificationRequest request);


}
