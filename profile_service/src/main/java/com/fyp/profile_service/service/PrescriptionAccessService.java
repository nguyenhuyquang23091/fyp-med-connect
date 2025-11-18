package com.fyp.profile_service.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.fyp.profile_service.constant.AccessStatus;
import com.fyp.profile_service.dto.request.PrescriptionNotification;
import com.fyp.profile_service.dto.response.PrescriptionAccessResponse;
import com.fyp.profile_service.entity.PrescriptionAccess;
import com.fyp.profile_service.exceptions.AppException;
import com.fyp.profile_service.exceptions.ErrorCode;
import com.fyp.profile_service.mapper.PrescriptionAccessMapper;
import com.fyp.profile_service.repository.PrescriptionAccessRepository;
import com.fyp.profile_service.repository.httpClient.NotificationFeignClient;
import com.fyp.profile_service.utils.ProfileServiceUtil;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PrescriptionAccessService {
    PrescriptionAccessRepository accessRequestRepository;
    PrescriptionAccessMapper prescriptionAccessMapper;
    NotificationFeignClient notificationFeignClient;

    public PrescriptionAccessResponse createRequest(PrescriptionNotification prescriptionNotification) {
        Instant now = Instant.now();

        PrescriptionAccess prescriptionAccess = PrescriptionAccess.builder()
                .accessStatus(AccessStatus.PENDING)
                .prescriptionId(prescriptionNotification.getPrescriptionId())
                .prescriptionName(prescriptionNotification.getPrescriptionName())
                .doctorUserId(prescriptionNotification.getDoctorUserId())
                .patientUserId(prescriptionNotification.getPatientUserId())
                .requestReason(prescriptionNotification.getRequestReason())
                .requestedAt(now)
                .expiresAt(now)
                .build();

        prescriptionAccess = accessRequestRepository.save(prescriptionAccess);
        PrescriptionNotification notificationRequest = PrescriptionNotification.accessRequest(prescriptionAccess);
        notificationFeignClient.sendPrescriptionNotification(notificationRequest);
        return prescriptionAccessMapper.toPrescriptionResponse(prescriptionAccess);
    }

    public PrescriptionAccessResponse approveRequest(String requestId) {
        String patientId = ProfileServiceUtil.getCurrentUserId();
        PrescriptionAccess prescriptionAccess = accessRequestRepository
                .findByIdAndPatientUserId(requestId, patientId)
                .orElseThrow(() -> new AppException(ErrorCode.REQUEST_NOTFOUND));

        if (!AccessStatus.PENDING.equals(prescriptionAccess.getAccessStatus())) {
            throw new AppException(ErrorCode.REQUEST_ALREADY_PROCESSED);
        }

        prescriptionAccess.setAccessStatus(AccessStatus.APPROVED);
        prescriptionAccess.setRespondedAt(Instant.now());

        PrescriptionNotification acceptNotificationRequest =
                PrescriptionNotification.accessApproved(prescriptionAccess);

        notificationFeignClient.sendPrescriptionNotification(acceptNotificationRequest);

        notificationFeignClient.markNotificationAsProcessed(patientId, requestId);

        return prescriptionAccessMapper.toPrescriptionResponse(accessRequestRepository.save(prescriptionAccess));
    }

    public PrescriptionAccessResponse denyRequest(String requestId) {
        String patientId = ProfileServiceUtil.getCurrentUserId();
        PrescriptionAccess prescriptionAccess = accessRequestRepository
                .findByIdAndPatientUserId(requestId, patientId)
                .orElseThrow(() -> new AppException(ErrorCode.REQUEST_NOTFOUND));

        if (!AccessStatus.PENDING.equals(prescriptionAccess.getAccessStatus())) {
            throw new AppException(ErrorCode.REQUEST_ALREADY_PROCESSED);
        }

        prescriptionAccess.setAccessStatus(AccessStatus.DENIED);
        prescriptionAccess.setRespondedAt(Instant.now());

        PrescriptionNotification denyNotificationRequest = PrescriptionNotification.accessDenied(prescriptionAccess);

        log.info("Current accessStatus is{} ", prescriptionAccess.getAccessStatus());
        notificationFeignClient.sendPrescriptionNotification(denyNotificationRequest);
        notificationFeignClient.markNotificationAsProcessed(patientId, requestId);
        return prescriptionAccessMapper.toPrescriptionResponse(accessRequestRepository.save(prescriptionAccess));
    }

    public boolean hasAccess(String doctorId, String prescriptionId, AccessStatus accessStatus) {
        return accessRequestRepository.existsPrescriptionAccessByDoctorUserIdAndPrescriptionIdAndAccessStatus(
                doctorId, prescriptionId, AccessStatus.APPROVED);
    }

    public AccessStatus getAccessStatusForDoctor(String doctorId, String prescriptionId) {
        return accessRequestRepository
                .findByDoctorUserIdAndPrescriptionId(doctorId, prescriptionId)
                .map(PrescriptionAccess::getAccessStatus)
                .orElse(AccessStatus.NO_REQUEST);
    }
}
