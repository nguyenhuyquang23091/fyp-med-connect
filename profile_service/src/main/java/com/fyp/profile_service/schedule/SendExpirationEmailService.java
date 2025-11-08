package com.fyp.profile_service.schedule;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fyp.profile_service.constant.AccessStatus;
import com.fyp.profile_service.entity.PrescriptionAccess;
import com.fyp.profile_service.entity.UserProfile;
import com.fyp.event.dto.NotificationEvent;
import com.fyp.profile_service.repository.PrescriptionAccessRepository;
import com.fyp.profile_service.repository.UserProfileRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SendExpirationEmailService {
    PrescriptionAccessRepository accessRepository;
    UserProfileRepository userProfileRepository;
    static final long _45_MINUTES_IN_SECONDS = 2700L;
    static final long _15_MINUTES_IN_MILLISECONDS = 900_000L;


    static final long _15_SECONDS = 15L;

    static final long _30_SECOND_IN_MILLISECONDS = 30000L;


    KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelay = _30_SECOND_IN_MILLISECONDS)
    public void sendExpirationEmailToDoctor() {
        try {
            log.info("Starting scheduled task: sendExpirationEmailToDoctor");

            Instant now = Instant.now();
            Instant _45MinutesAgo = now.minusSeconds(_45_MINUTES_IN_SECONDS);

            Instant _15SecondAgo = now.minusSeconds(_15_SECONDS);

            List<PrescriptionAccess> inactiveRequests =
                    accessRepository.findAllByAccessStatusAndRequestedAtBefore(AccessStatus.PENDING, _15SecondAgo);

            if (inactiveRequests.isEmpty()) {
                log.info("No pending prescription access requests found that are older than 45 minutes");
                return;
            }

            log.info("Found {} pending prescription access requests to process", inactiveRequests.size());

            Set<String> doctorUserIds = inactiveRequests.stream()
                    .map(PrescriptionAccess::getDoctorUserId)
                    .collect(Collectors.toSet());

            // Extract unique patient IDs to batch fetch profiles
            Set<String> patientUserIds = inactiveRequests.stream()
                    .map(PrescriptionAccess::getPatientUserId)
                    .collect(Collectors.toSet());

            // Batch fetch all doctor and patient profiles in parallel
            List<UserProfile> doctorProfiles = userProfileRepository.findAllByUserIdIn(doctorUserIds);
            List<UserProfile> patientProfiles = userProfileRepository.findAllByUserIdIn(patientUserIds);

            // Create Maps for O(1) lookup of profiles
            Map<String, UserProfile> doctorProfileMap =
                    doctorProfiles.stream().collect(Collectors.toMap(UserProfile::getUserId, profile -> profile));
            Map<String, UserProfile> patientProfileMap =
                    patientProfiles.stream().collect(Collectors.toMap(UserProfile::getUserId, profile -> profile));

            int sentCount = 0;
            int skippedCount = 0;

            for (PrescriptionAccess prescriptionAccess : inactiveRequests) {
                try {
                    UserProfile doctorProfile = doctorProfileMap.get(prescriptionAccess.getDoctorUserId());

                    // Validate doctor profile exists and has email
                    if (doctorProfile == null) {
                        log.warn("Doctor profile not found for userId: {}", prescriptionAccess.getDoctorUserId());
                        skippedCount++;
                        continue;
                    }

                    String doctorEmail = doctorProfile.getEmail();
                    if (doctorEmail == null || doctorEmail.isBlank()) {
                        log.warn("Doctor {} has no email address", doctorProfile.getUserId());
                        skippedCount++;
                        continue;
                    }

                    // Get patient profile for full name
                    UserProfile patientProfile = patientProfileMap.get(prescriptionAccess.getPatientUserId());
                    if (patientProfile == null) {
                        log.warn("Patient profile not found for userId: {}", prescriptionAccess.getPatientUserId());
                        skippedCount++;
                        continue;
                    }

                    // Build notification event
                    Map<String, Object> params =
                            buildParam(prescriptionAccess, doctorProfile, patientProfile);

                    NotificationEvent notificationEvent = NotificationEvent.builder()
                            .channel("EMAIL")
                            .recipientEmail(doctorEmail)
                            .templateCode(4L)
                            .param(params)
                            .build();

                    // Send to Kafka with async callback for error handling
                    CompletableFuture<SendResult<String, Object>> future =
                            kafkaTemplate.send("notification-delivery", notificationEvent);

                    future.whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error(
                                    "Failed to send expiration notification to doctor {} via Kafka: {}",
                                    doctorProfile.getUserId(),
                                    ex.getMessage(),
                                    ex);
                        } else {
                            log.debug(
                                    "Successfully sent expiration notification to doctor {} at {}",
                                    doctorProfile.getUserId(),
                                    doctorEmail);
                        }
                    });
                    sentCount++;
                } catch (Exception e) {
                    log.error(
                            "Unexpected error processing prescription access for doctor {}: {}",
                            prescriptionAccess.getDoctorUserId(),
                            e.getMessage(),
                            e);
                    skippedCount++;
                }
            }
            log.info("Completed sendExpirationEmailToDoctor task. Sent: {}, Skipped: {}", sentCount, skippedCount);
        } catch (Exception e) {
            log.error("Critical error in sendExpirationEmailToDoctor scheduled task: {}", e.getMessage(), e);
        }
    }

    private static Map<String, Object> buildParam(PrescriptionAccess prescriptionAccess, UserProfile doctorProfile, UserProfile patientProfile) {
        String doctorFullName = doctorProfile.getFirstName() + " " + doctorProfile.getLastName();
        String patientFullName = patientProfile.getFirstName() + " " + patientProfile.getLastName();
        String prescriptionName = prescriptionAccess.getPrescriptionName();

        Map<String, Object> params = new HashMap<>();
        params.put("doctorFullName", doctorFullName);
        params.put("patientFullName", patientFullName);
        params.put("prescriptionName", prescriptionName);
        return params;
    }
}
