package com.fyp.appointment_service.controller;


import com.fyp.appointment_service.dto.request.ApiResponse;
import com.fyp.appointment_service.dto.request.AppointmentRequest;
import com.fyp.appointment_service.dto.request.AppointmentUpdateRequest;
import com.fyp.appointment_service.dto.request.PaymentRequest;
import com.fyp.appointment_service.dto.response.AppointmentResponse;
import com.fyp.appointment_service.dto.response.PageResponse;
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


    @GetMapping("/my-appointments")
    public ApiResponse<PageResponse<AppointmentResponse>> getMyAppointments(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ){
        return ApiResponse.<PageResponse<AppointmentResponse>>builder()
                .result(appointmentService.getMyAppointment(page, size))
                .build();
    }


    @GetMapping("/my-appointments/{appointmentId}")
    public ApiResponse<AppointmentResponse> getOneAppointments(@PathVariable @Valid String appointmentId){
        return ApiResponse.<AppointmentResponse>builder()
                .result(appointmentService.getOneAppointment(appointmentId)).build();
    }


    @GetMapping("/my-upcoming-appointments")
    public ApiResponse<PageResponse<AppointmentResponse>> getMyUpcomingAppointments(

            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "5") int size
    ){
        return ApiResponse.<PageResponse<AppointmentResponse>>builder()
                .result(appointmentService.getMyUpcomingAppointments(page, size))
                .build();
    }


    @PutMapping("/cancel/{appointmentId}")
    public ApiResponse<AppointmentResponse> cancelMyAppointment(@PathVariable @Valid String appointmentId){
        return ApiResponse.<AppointmentResponse>builder()
                .result(appointmentService.cancelMyAppointment(appointmentId))
                .build();
    }

    @PutMapping("/update/{appointmentId}")
    public ApiResponse<AppointmentResponse> updateMyAppointment(@PathVariable @Valid String appointmentId, @RequestBody @Valid AppointmentUpdateRequest appointmentUpdateRequest){
        return ApiResponse.<AppointmentResponse>builder()
                .result(appointmentService.updateMyAppointment(appointmentId, appointmentUpdateRequest))
                .build();
    }

    @DeleteMapping()
    public ApiResponse<Void> deleteMyAppointment(){
        appointmentService.deleteMyAppointment();
        return ApiResponse.<Void>builder()
                .message("Appointment deleted successfully")
                .build();
    }


    //DOCTOR ENDPOINT
    @GetMapping("/get-doctor-appointments")
    public ApiResponse<PageResponse<AppointmentResponse>> getDoctorAppointment(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ){
        return ApiResponse.<PageResponse<AppointmentResponse>>builder()
                .result(appointmentService.getDoctorAppointment(page, size))
                .build();
    }


    @GetMapping("/patient-upcoming-appointments/{patientId}")
    public ApiResponse<PageResponse<AppointmentResponse>> getPatientUpcomingAppointments(

            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "5") int size,
            @PathVariable @Valid String patientId

    ){
        return ApiResponse.<PageResponse<AppointmentResponse>>builder()
                .result(appointmentService.getOnePatientUpcomingAppointment(patientId, page, size))
                .build();


    }




 }
