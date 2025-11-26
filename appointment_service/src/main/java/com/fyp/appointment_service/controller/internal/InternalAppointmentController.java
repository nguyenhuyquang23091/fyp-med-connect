package com.fyp.appointment_service.controller.internal;


import com.fyp.appointment_service.dto.request.ApiResponse;
import com.fyp.appointment_service.dto.response.AppointmentResponse;
import com.fyp.appointment_service.service.AppointmentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalAppointmentController {
    AppointmentService appointmentService;
    @PutMapping("/{appointmentId}/complete")
    public ApiResponse<AppointmentResponse> markComplete(@PathVariable String appointmentId) {
        return ApiResponse.<AppointmentResponse>builder()
                .result(appointmentService.markAppointmentAsComplete(appointmentId))
                .build();
    }
}
