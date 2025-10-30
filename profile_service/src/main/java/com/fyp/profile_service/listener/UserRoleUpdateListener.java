package com.fyp.profile_service.listener;

import java.time.LocalDateTime;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fyp.event.dto.UserRoleUpdateEvent;
import com.fyp.profile_service.entity.DoctorProfile;
import com.fyp.profile_service.entity.UserProfile;
import com.fyp.profile_service.exceptions.AppException;
import com.fyp.profile_service.exceptions.ErrorCode;
import com.fyp.profile_service.repository.DoctorProfileRepository;
import com.fyp.profile_service.repository.UserProfileRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserRoleUpdateListener {

    UserProfileRepository userProfileRepository;
    DoctorProfileRepository doctorProfileRepository;


    private static final String DOCTOR_ROLE = "DOCTOR";

    KafkaTemplate<String, Object> kafkaTemplate;


    @KafkaListener(topics = "user-role-updated", groupId = "profile-service-group")
    @Transactional
    public void handleUserRoleUpdate(UserRoleUpdateEvent event) {
        log.info(
                "Received role update event for user: {} - Old roles: {}, New roles: {}",
                event.getUserId(),
                event.getOldRoles(),
                event.getNewRoles());
        try {
            boolean doctorRoleAdded = event.getNewRoles().contains(DOCTOR_ROLE)
                    && !event.getOldRoles().contains(DOCTOR_ROLE);

            // Check if DOCTOR role was removed
            boolean doctorRoleRemoved = event.getOldRoles().contains(DOCTOR_ROLE)
                    && !event.getNewRoles().contains(DOCTOR_ROLE);

            if (doctorRoleAdded) {
                handleDoctorRoleAdded(event.getUserId(), event.getEmail());
            } else if (doctorRoleRemoved) {
                handleDoctorRoleRemoved(event.getUserId());
            }

        } catch (Exception e) {
            log.error("Error processing role update event for user {}: {}", event.getUserId(), e.getMessage(), e);
            throw e; // Rethrow to trigger Kafka retry mechanism
        }
    }

    private void handleDoctorRoleAdded(String userId, String email) {
        log.info("User {} promoted to DOCTOR role, creating DoctorProfile", userId);

        // Find or create UserProfile
        UserProfile userProfile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (doctorProfileRepository.existsByUserId(userId)) {
            log.info("DoctorProfile already exists for userId: {}, skipping creation", userId);
            return;
        }
        DoctorProfile doctorProfile = DoctorProfile.builder()
                .userId(userId)
                .isAvailable(false) // Default to not available until doctor completes profile
                .yearsOfExperience(0)
                .bio("")
                .residency("")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        doctorProfile = doctorProfileRepository.save(doctorProfile);

        userProfile.setDoctorProfile(doctorProfile);
        userProfileRepository.save(userProfile);


        log.info("Successfully created and linked DoctorProfile for user: {}", userId);
    }

    private void handleDoctorRoleRemoved(String userId) {
        log.info("User {} demoted from DOCTOR role, updating DoctorProfile", userId);

        doctorProfileRepository.findByUserId(userId).ifPresent(doctorProfile -> {
            // Option 1: Mark as unavailable (preserves data)
            doctorProfile.setIsAvailable(false);
            doctorProfile.setUpdatedAt(LocalDateTime.now());
            doctorProfileRepository.save(doctorProfile);

            log.info("Marked DoctorProfile as unavailable for user: {}", userId);

            // Option 2: Remove relationship from UserProfile but keep DoctorProfile
            userProfileRepository.findByUserId(userId).ifPresent(userProfile -> {
                userProfile.setDoctorProfile(null);
                userProfileRepository.save(userProfile);
            });

            // Option 3: Delete DoctorProfile entirely (use with caution - loses historical data)
            // doctorProfileRepository.delete(doctorProfile);
        });
    }
}
