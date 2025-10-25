package com.fyp.appointment_service.controller;


import com.fyp.appointment_service.dto.request.ApiResponse;
import com.fyp.appointment_service.dto.request.AppointmentRequest;
import com.fyp.appointment_service.dto.request.PaymentRequest;
import com.fyp.appointment_service.dto.response.AppointmentResponse;
import com.fyp.appointment_service.dto.response.PaymentResponse;
import com.fyp.appointment_service.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping()
@RequiredArgsConstructor
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AppointmentController {

    AppointmentService appointmentService;
    @PostMapping("/create")
    public ApiResponse<AppointmentResponse> createAppointment(@Valid @RequestBody AppointmentRequest request){
        return ApiResponse.<AppointmentResponse>builder()
                .result( appointmentService.createAppointment(request))
                .build();
    }



    @GetMapping()
    public List<AppointmentResponse> getDoctorAppointment(){
        return appointmentService.getDoctorAppointment();
    }


    @GetMapping("/my-appointments")
    public ApiResponse<List<AppointmentResponse>> getMyAppointments(){
        return ApiResponse.<List<AppointmentResponse>>builder()
                .result(appointmentService.getMyAppointment())
                .build();
    }
    @PutMapping("/cancel")
    public ApiResponse<AppointmentResponse> cancelMyAppointment(){
        return ApiResponse.<AppointmentResponse>builder()
                .result(appointmentService.cancelMyAppointment())
                .build();
    }


    @DeleteMapping()
    public ApiResponse<Void> deleteMyAppointment(){
        appointmentService.deleteMyAppointment();
        return ApiResponse.<Void>builder()
                .message("Appointment deleted successfully")
                .build();
    }
 }
