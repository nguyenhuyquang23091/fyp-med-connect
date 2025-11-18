package com.fyp.appointment_service.repository.httpCLient;

import com.fyp.appointment_service.configuration.AuthenticationInterceptor;
import com.fyp.appointment_service.dto.request.ApiResponse;
import com.fyp.appointment_service.dto.request.AppointmentNotificationRequest;
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
            value = "/send-appointments-notification",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<String> sendAppointmentNotification(@RequestBody AppointmentNotificationRequest request);


}
