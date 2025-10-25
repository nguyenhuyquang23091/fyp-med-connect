package com.fyp.appointment_service.service;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.fyp.appointment_service.constant.AppointmentStatus;
import com.fyp.appointment_service.dto.request.PaymentRequest;
import com.fyp.appointment_service.dto.response.*;
import com.fyp.appointment_service.exceptions.AppException;
import com.fyp.appointment_service.exceptions.ErrorCode;
import com.fyp.appointment_service.repository.httpCLient.ProfileClient;
import com.fyp.appointment_service.repository.httpCLient.VnPayClient;
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
    VnPayClient vnPayClient;

    public AppointmentResponse createAppointment(AppointmentRequest request){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
       String userId = authentication.getName();

        DoctorProfileResponse doctorProfileResponse = profileClient.getOneDoctorProfile(request.getDoctorId()).getResult(
        );
        UserProfileResponse userProfileResponse = profileClient.getMyProfile().getResult();

        String patientFullName = userProfileResponse.getFirstName() + " " + userProfileResponse.getLastName();
        String doctorFullName = doctorProfileResponse.getFirstName() + " " + doctorProfileResponse.getLastName();

        //  We will use stream and filter here to filter doctor
        // specialty which match which selected one from patient
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

        appointmentRepository.save(appointmentEntity);

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .referenceId(appointmentEntity.getId())
                .amount(price)
                .build();

        PaymentResponse paymentResponse = vnPayClient.createVnPayment(paymentRequest).getResult();

        String paymentUrl  = paymentResponse.getPaymentUrl();
        AppointmentResponse appointmentResponse =  appointmentMapper.toAppointmentRespone(appointmentEntity);
        appointmentResponse.setPaymentURL(paymentUrl);

        return  appointmentResponse;
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('VIEW_DOCTOR_PROFILES')")
    public List<DoctorProfileResponse> getAllDoctorProfile(){
        return profileClient.getAllAvailableDoctor().getResult();
    }

    public AppointmentResponse cancelMyAppointment(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        // Fetch the appointment
        AppointmentEntity appointment = appointmentRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        // Check if already cancelled
        if (appointment.getAppointmentStatus() == AppointmentStatus.CANCELLED) {
            throw new AppException(ErrorCode.APPOINTMENT_ALREADY_CANCELLED);
        }
        // Check if already completed
        if (appointment.getAppointmentStatus() == AppointmentStatus.COMPLETED) {
            throw new AppException(ErrorCode.APPOINTMENT_ALREADY_COMPLETED);
        }
        // Update status to CANCELLED
        appointment.setAppointmentStatus(AppointmentStatus.CANCELLED);
        appointment.setModifiedDate(Instant.now());
        AppointmentEntity updatedAppointment = appointmentRepository.save(appointment);

        return appointmentMapper.toAppointmentRespone(updatedAppointment);
    }


    public List<AppointmentResponse> getMyAppointment(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        return appointmentRepository
                .findAllByUserId(userId)
                .stream().map(appointmentMapper::toAppointmentRespone)
                .toList();
    }

    public void deleteMyAppointment(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        AppointmentEntity appointmentEntity =
                appointmentRepository.findByUserId(userId).orElseThrow(() -> new AppException((ErrorCode.APPOINTMENT_NOT_FOUND)));
        // Delete the appointment
        appointmentRepository.deleteById(appointmentEntity.getId());
    }

    //DOCTOR AUTHORITIES

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
