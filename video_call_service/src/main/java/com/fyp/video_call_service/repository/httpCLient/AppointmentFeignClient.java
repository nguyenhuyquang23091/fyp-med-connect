package com.fyp.video_call_service.repository.httpCLient;


import com.fyp.video_call_service.configuration.AuthenticationInterceptor;
import com.fyp.video_call_service.dto.request.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;


@FeignClient(
        name = "appointment-service",
        url = "http://localhost:8084/appointment",
        configuration = AuthenticationInterceptor.class)
public interface AppointmentFeignClient {

    @PutMapping(
            value = "/internal/{appointmentId}/complete",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Object> markAppointmentComplete(@PathVariable("appointmentId") String appointmentId);
}
