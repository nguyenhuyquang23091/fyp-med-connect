package com.profile.profile_service.utils;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import com.profile.profile_service.entity.UserPrescription;
import com.profile.profile_service.exceptions.AppException;
import com.profile.profile_service.exceptions.ErrorCode;
import com.profile.profile_service.repository.UserPrescriptionRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CacheEvictionUtil {

    UserPrescriptionRepository userPrescriptionRepository;

    @Caching(
            evict = {
                @CacheEvict(value = "PRESCRIPTION_LIST_CACHE", key = "#result"),
                @CacheEvict(value = "PRESCRIPTION_DETAIL_CACHE", key = "#prescriptionId")
            })
    public String evictPrescriptionCaches(String prescriptionId) {
        UserPrescription prescription = userPrescriptionRepository
                .findById(prescriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        String patientId = prescription.getUserId();
        log.info("Evicting prescription caches for patient: {} and prescription: {}", patientId, prescriptionId);
        return patientId;
    }

    @Caching(
            evict = {
                @CacheEvict(value = "PRESCRIPTION_LIST_CACHE", key = "#patientId"),
                @CacheEvict(value = "PRESCRIPTION_DETAIL_CACHE", key = "#prescriptionId")
            })
    public void evictPrescriptionCaches(String patientId, String prescriptionId) {
        log.info("Evicting prescription caches for patient: {} and prescription: {}", patientId, prescriptionId);
    }
}
