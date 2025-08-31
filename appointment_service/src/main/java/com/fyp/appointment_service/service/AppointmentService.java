package com.fyp.appointment_service.service;


import java.time.Instant;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fyp.appointment_service.dto.request.AppointmentRequest;
import com.fyp.appointment_service.dto.response.AppointmentResponse;
import com.fyp.appointment_service.entity.AppointmentEntity;
import com.fyp.appointment_service.mapper.AppointmentMapper;
import com.fyp.appointment_service.repository.AppointmentRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentService {
    AppointmentRepository appointmentRepository;
    AppointmentMapper appointmentMapper;
    public AppointmentResponse createAppointment(AppointmentRequest request){
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
       String userId = authentication.getName();
        AppointmentEntity appointmentEntity = AppointmentEntity.builder()
                .userId(userId)
                .note(request.getNote())
                .reasons(request.getReasons())
                .appointment_date(request.getAppointment_date())
                .doctorId(request.getDoctorId())
                .createdDate(Instant.now())
                .modifiedDate(Instant.now())
                .build();
        return appointmentMapper.toAppointmentRespone(appointmentRepository.save(appointmentEntity));
    }

    public List<AppointmentResponse> getMyAppointment(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        return appointmentRepository
                .findAllByUserId(userId)
                .stream().map(appointmentMapper::toAppointmentRespone)
                .toList();
    }


    public void deleteMyAppointment(String id){
         appointmentRepository.deleteById(id);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    public List<AppointmentResponse> getDoctorAppointment(){
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
       String doctorIdJWT = authentication.getName();
                return appointmentRepository
                        .findAllByDoctorId(doctorIdJWT)
                        .stream().map(appointmentMapper::toAppointmentRespone)
                        .toList();
    }


}
