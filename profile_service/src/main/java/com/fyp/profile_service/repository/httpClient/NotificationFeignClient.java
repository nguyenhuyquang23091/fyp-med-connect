package com.fyp.profile_service.repository.httpClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fyp.profile_service.config.AuthenticationInterceptor;
import com.fyp.profile_service.dto.request.ApiResponse;
import com.fyp.profile_service.dto.request.PrescriptionNotification;
import com.fyp.profile_service.dto.response.NotificationResponse;

@FeignClient(
        name = "notification-service",
        url = "http://localhost:8085/notifications",
        configuration = AuthenticationInterceptor.class)
public interface NotificationFeignClient {
    @PostMapping(
            value = "/send-prescription-notification",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<String> sendPrescriptionNotification(@RequestBody PrescriptionNotification request);

    @PostMapping(
            value = "/email/send",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<String> sendEmail(@RequestBody PrescriptionNotification request);

    @PutMapping(value = "/internal/mark-processed/{recipientUserId}/{requestId}")
    ApiResponse<NotificationResponse> markNotificationAsProcessed(
            @PathVariable("recipientUserId") String recipientUserId, @PathVariable("requestId") String requestId);
}
