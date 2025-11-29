package com.fyp.appointment_service.service;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fyp.appointment_service.constant.AppointmentStatus;
import com.fyp.appointment_service.constant.ConsultationType;
import com.fyp.appointment_service.dto.request.AppointmentNotificationRequest;
import com.fyp.appointment_service.dto.request.AppointmentUpdateRequest;
import com.fyp.appointment_service.dto.request.PaymentRequest;
import com.fyp.appointment_service.dto.response.*;
import com.fyp.appointment_service.exceptions.AppException;
import com.fyp.appointment_service.exceptions.ErrorCode;
import com.fyp.appointment_service.repository.httpCLient.NotificationFeignClient;
import com.fyp.appointment_service.repository.httpCLient.ProfileClient;
import com.fyp.appointment_service.repository.httpCLient.VnPayClient;
import event.dto.AppointmentEvent;
import event.dto.SessionVideoEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
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
    NotificationFeignClient notificationFeignClient;

    KafkaTemplate<String, Object> kafkaTemplate;

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
                .paymentMethod(request.getPaymentMethod())
                .appointmentStatus(AppointmentStatus.UPCOMING)
                .createdDate(Instant.now())
                .build();

        appointmentRepository.save(appointmentEntity);

         AppointmentResponse appointmentResponse = handlePaymentMethod(appointmentEntity, price);

        publishAppointmentEvents(appointmentEntity);

        return  appointmentResponse;

    }

    private AppointmentResponse handlePaymentMethod(AppointmentEntity appointmentEntity, BigDecimal price){


            //using switch for future expansion of payment method

        AppointmentResponse appointmentResponse =   appointmentMapper.toAppointmentRespone(appointmentEntity);

        switch (appointmentEntity.getPaymentMethod()) {
            case VN_PAY -> {
                PaymentRequest paymentRequest = PaymentRequest.builder()
                        .referenceId(appointmentEntity.getId())
                        .amount(price)
                        .build();

                PaymentResponse paymentResponse = vnPayClient.createVnPayment(paymentRequest).getResult();
                appointmentResponse.setPaymentURL(paymentResponse.getPaymentUrl());
            }
            case IN_CASH -> {
                    //do nothing
            }

            default -> {

                throw new AppException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
            }
        }

        return  appointmentResponse;
    }

    private void publishAppointmentEvents(AppointmentEntity appointmentEntity){

        AppointmentEvent appointmentCreatedEvent = AppointmentEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .appointmentId(appointmentEntity.getId())
                .userId(appointmentEntity.getUserId())
                .doctorId(appointmentEntity.getDoctorId())
                .specialty(appointmentEntity.getSpecialty())
                .consultationType(appointmentEntity.getConsultationType().name())
                .appointmentStatus(appointmentEntity.getAppointmentStatus().name())
                .price(appointmentEntity.getPrices())
                .appointmentDateTime(appointmentEntity.getAppointmentDateTime().toString())
                .createdAt(appointmentEntity.getCreatedDate().toString())
                .build();

        kafkaTemplate.send("appointment-created", appointmentCreatedEvent);
        log.info("Published AppointmentCreatedEvent for appointment: {}", appointmentEntity.getId());


        if (appointmentEntity.getConsultationType() == ConsultationType.VIDEO_CALL){

            SessionVideoEvent sessionVideoEvent =
                    SessionVideoEvent.builder()
                            .roomId(appointmentEntity.getId())
                            .appointmentId(appointmentEntity.getId())
                            .scheduledTime(appointmentEntity.getAppointmentDateTime().toString())
                            .patientId(appointmentEntity.getUserId())
                            .doctorId(appointmentEntity.getDoctorId())
                    .build();

            kafkaTemplate.send("video-call-events", sessionVideoEvent);

        }
    }

    public AppointmentResponse markAppointmentAsComplete(String appointmentId){

        AppointmentEntity appointment = appointmentRepository
                .findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        validateAppointmentStatus(appointment);

        appointment.setAppointmentStatus(AppointmentStatus.COMPLETED);
        appointment.setModifiedDate(Instant.now());

        AppointmentEntity updatedAppointment = appointmentRepository.save(appointment);

        return appointmentMapper.toAppointmentRespone(updatedAppointment);
    }

    public AppointmentResponse cancelMyAppointment(String appointmentId){

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        AppointmentEntity appointment = appointmentRepository
                .findByUserIdAndId(userId, appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        validateAppointmentStatus(appointment);

        // Update status to CANCELLED
        appointment.setAppointmentStatus(AppointmentStatus.CANCELLED);
        appointment.setModifiedDate(Instant.now());
        AppointmentEntity updatedAppointment = appointmentRepository.save(appointment);
        // Send notification to doctor about appointment cancellation
        AppointmentNotificationRequest notification =
                AppointmentNotificationRequest.cancelledAppointment(updatedAppointment, updatedAppointment.getDoctorId());
        notificationFeignClient.sendAppointmentNotification(notification);

        return appointmentMapper.toAppointmentRespone(updatedAppointment);
    }

    private AppointmentEntity validateAppointmentStatus(AppointmentEntity appointment){


        if (appointment.getAppointmentStatus() == AppointmentStatus.CANCELLED) {
            throw new AppException(ErrorCode.APPOINTMENT_ALREADY_CANCELLED);
        }
        if (appointment.getAppointmentStatus() == AppointmentStatus.COMPLETED) {
            throw new AppException(ErrorCode.APPOINTMENT_ALREADY_COMPLETED);
        }

        return appointment;
    }


    public PageResponse<AppointmentResponse> getMyAppointment(int page, int size){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        Sort sort = Sort.by("createdDate").descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<AppointmentEntity> appointments = appointmentRepository.findAllByUserId(userId, pageable);

        return PageResponse.<AppointmentResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(appointments.getTotalPages())
                .totalElements(appointments.getTotalElements())
                .data(appointments.getContent().stream()
                        .map(appointmentMapper::toAppointmentRespone)
                        .toList())
                .build();
    }

    public AppointmentResponse updateMyAppointment(String appointmentId, AppointmentUpdateRequest request){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        AppointmentEntity appointmentEntity =
                appointmentRepository.findByUserIdAndId(userId, appointmentId)
                        .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        appointmentMapper.updateAppointment(appointmentEntity, request);

        appointmentEntity.setModifiedDate(Instant.now());

        appointmentRepository.save(appointmentEntity);

        // Send notification to doctor about appointment update
        AppointmentNotificationRequest notification =
                AppointmentNotificationRequest.updatedAppointment(appointmentEntity, appointmentEntity.getDoctorId());
        notificationFeignClient.sendAppointmentNotification(notification);

        return  appointmentMapper.toAppointmentRespone(appointmentEntity);

    }

    //used for both doctor, patient
    public AppointmentResponse getOneAppointment(String appointmentId){
        //Add get one doctor profile vao
        // use for displaying doctor avatar
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        AppointmentEntity appointment = appointmentRepository
                .findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));


        if (!appointment.getUserId().equals(userId) && !appointment.getDoctorId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return  appointmentMapper.toAppointmentRespone(appointment);

    }



    public void deleteMyAppointment(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        AppointmentEntity appointmentEntity =
                appointmentRepository.findByUserId(userId).orElseThrow(() -> new AppException((ErrorCode.APPOINTMENT_NOT_FOUND)));
        // Delete the appointment
        appointmentRepository.deleteById(appointmentEntity.getId());
    }


    public PageResponse<AppointmentResponse> getMyUpcomingAppointments(int page, int size){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        return getUpcomingAppointmentsByUserId(userId, page, size);
    }




    //DOCTOR AUTHORITIES

    @PreAuthorize("hasRole('DOCTOR')")
    public PageResponse<AppointmentResponse> getDoctorAppointment(int page, int size){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String doctorIdJWT = authentication.getName();

        Sort sort = Sort.by("appointmentDateTime").descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<AppointmentEntity> appointments = appointmentRepository.findAllByDoctorId(doctorIdJWT, pageable);

        return PageResponse.<AppointmentResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(appointments.getTotalPages())
                .totalElements(appointments.getTotalElements())
                .data(appointments.getContent().stream()
                        .map(appointmentMapper::toAppointmentRespone)
                        .toList())
                .build();
    }

    @PreAuthorize("hasRole('DOCTOR')")
    public PageResponse<AppointmentResponse> getOnePatientUpcomingAppointment(String userId, int page, int size){
        return getUpcomingAppointmentsByUserId(userId, page, size);
    }

    private PageResponse<AppointmentResponse> getUpcomingAppointmentsByUserId(String userId, int page, int size){
        Sort sort = Sort.by("appointmentDateTime").ascending();
        Pageable limit = PageRequest.of(page - 1, size, sort);
        var appointment = appointmentRepository.findByUserIdAndAppointmentDateTimeAfterAndAppointmentStatus
                (userId, LocalDateTime.now(), AppointmentStatus.UPCOMING, limit);
        return PageResponse.<AppointmentResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(appointment.getTotalPages())
                .totalElements(appointment.getTotalElements())
                .data(appointment.getContent().stream().map(appointmentMapper::toAppointmentRespone).toList())
                .build();
    }
}
