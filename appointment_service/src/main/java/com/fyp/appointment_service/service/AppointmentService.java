package com.fyp.appointment_service.service;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.fyp.appointment_service.constant.AppointmentStatus;
import com.fyp.appointment_service.dto.response.*;
import com.fyp.appointment_service.exceptions.AppException;
import com.fyp.appointment_service.exceptions.ErrorCode;
import com.fyp.appointment_service.repository.httpCLient.ProfileClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fyp.appointment_service.dto.request.AppointmentRequest;
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
    ProfileClient profileClient;

    public AppointmentResponse createAppointment(AppointmentRequest request){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
       String userId = authentication.getName();

        DoctorProfileResponse doctorProfileResponse = profileClient.getOneDoctorProfile(request.getDoctorId()).getResult(

        );
        UserProfileResponse userProfileResponse = profileClient.getMyProfile().getResult();

        String patientFullName = userProfileResponse.getFirstName() + " " + userProfileResponse.getLastName();
        String doctorFullName = doctorProfileResponse.getFirstName() + " " + doctorProfileResponse.getLastName();

        //We will use stream and filter here to filter doctor specialty which match which selected one from patient
        DoctorSpecialtyResponse selectedSpecialty =
                doctorProfileResponse.getSpecialties()
                        .stream()
                        .filter(s -> s.getRelationshipId().equals(request.getSpecialtyRelationshipId())).findFirst()
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID_SPECIALTY_SELECTION));

        DoctorServiceResponse selectedService =
                doctorProfileResponse
                        .getServices().stream()
                        .filter(s -> s.getRelationshipId().equals(request.getServiceRelationshipId())).findFirst()
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID_SERVICE_SELECTION));

        String specialtyName = selectedSpecialty.getSpecialty().getName();
        String serviceName = selectedService.getService().getName();
        BigDecimal price = selectedService.getPrice();

        AppointmentEntity appointmentEntity = AppointmentEntity.builder()
                .userId(userId)
                .doctorId(request.getDoctorId())
                .patientFullName(patientFullName)
                .doctorFullName(doctorFullName)
                .reasons(request.getReasons())
                .phoneNumber(request.getPhoneNumber())
                .appointmentDateTime(request.getAppointmentDateTime())
                .specialty(specialtyName)
                .services(serviceName)
                .prices(price)
                .consultationType(request.getConsultationType())
                .appointmentStatus(AppointmentStatus.UPCOMING)
                .createdDate(Instant.now())
                .build();
        return appointmentMapper.toAppointmentRespone(appointmentRepository.save(appointmentEntity));
    }


    public List<AppointmentResponse> getMyAppointment(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
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
        var authentication = SecurityContextHolder.getContext().getAuthentication();
       String doctorIdJWT = authentication.getName();
                return appointmentRepository
                        .findAllByDoctorId(doctorIdJWT)
                        .stream().map(appointmentMapper::toAppointmentRespone)
                        .toList();
    }


}
