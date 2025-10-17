package com.fyp.profile_service.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fyp.profile_service.constant.AccessStatus;
import com.fyp.profile_service.constant.PrescriptionStatus;
import com.fyp.profile_service.dto.request.DoctorPrescriptionUpdateRequest;
import com.fyp.profile_service.dto.request.PatientPrescriptionCreateRequest;
import com.fyp.profile_service.dto.request.PatientPrescriptionUpdateRequest;
import com.fyp.profile_service.dto.request.PrescriptionNotification;
import com.fyp.profile_service.dto.response.PrescriptionAccessResponse;
import com.fyp.profile_service.dto.response.PrescriptionGeneralResponse;
import com.fyp.profile_service.dto.response.PrescriptionResponse;
import com.fyp.profile_service.entity.UserPrescription;
import com.fyp.profile_service.entity.UserProfile;
import com.fyp.profile_service.exceptions.AppException;
import com.fyp.profile_service.exceptions.ErrorCode;
import com.fyp.profile_service.mapper.UserPrescriptionMapper;
import com.fyp.profile_service.repository.UserPrescriptionRepository;
import com.fyp.profile_service.repository.UserProfileRepository;
import com.fyp.profile_service.repository.httpClient.FileFeignClient;
import com.fyp.profile_service.repository.httpClient.NotificationFeignClient;
import com.fyp.profile_service.utils.CacheEvictionUtil;
import com.fyp.profile_service.utils.ProfileServiceUtil;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserPrescriptionService {

    UserPrescriptionMapper userPrescriptionMapper;
    UserPrescriptionRepository repository;
    UserProfileRepository profileRepository;
    PrescriptionAccessService prescriptionAccessService;
    FileFeignClient fileService;
    NotificationFeignClient notificationService;
    AsyncServiceFileManagement asyncServiceFileManagement;
    CacheEvictionUtil cacheEvictionUtil;
    // User Prescription Management

    @CacheEvict(
            value = "PRESCRIPTION_LIST_CACHE",
            key = "T(com.fyp.profile_service.utils.ProfileServiceUtil).getCurrentUserId()")
    public PrescriptionResponse createPrescription(
            PatientPrescriptionCreateRequest request, List<MultipartFile> prescriptionImages) {

        String userId = ProfileServiceUtil.getCurrentUserId();
        UserProfile userProfile =
                profileRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        String userProfileId = userProfile.getId();

        // convert PrescriptionCreateRequest to PatientPrescriptionData
        // Convert PatientPrescriptionDataRequest to UserPrescription.PrescriptionData

        List<UserPrescription.PrescriptionData> prescriptionDataList = request.getPrescriptionData().stream()
                .map(patientData -> UserPrescription.PrescriptionData.builder()
                        .bloodSugarLevel(patientData.getBloodSugarLevel())
                        .readingType(patientData.getReadingType())
                        .measurementDate(patientData.getMeasurementDate())
                        .bloodSugarCategory(patientData.getBloodSugarCategory())
                        // Medical fields are null - to be filled by doctor later
                        .medicationName(null)
                        .dosage(null)
                        .frequency(null)
                        .instructions(null)
                        .doctorNotes(null)
                        .build())
                .collect(Collectors.toList());

        // Extract auth token from current request for async propagation
        String authToken = ProfileServiceUtil.getAuthorizationToken();

        // Upload images in parallel, passing auth token to each async thread
        List<CompletableFuture<String>> uploadFutures = prescriptionImages.stream()
                .map(image -> asyncServiceFileManagement.uploadImageAsync(image, authToken))
                .toList();

        // Wait for all uploads to complete
        CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0])).join();

        // Collect all uploaded URLs
        List<String> uploadedFileURls =
                uploadFutures.stream().map(CompletableFuture::join).toList();

        UserPrescription userPrescription = UserPrescription.builder()
                .userId(userId)
                .userProfileId(userProfileId)
                .prescriptionName(request.getPrescriptionName())
                .doctorId(null)
                .imageURLS(uploadedFileURls)
                .prescriptionData(prescriptionDataList)
                .status(PrescriptionStatus.PENDING_DOCTOR_REVIEW)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        userPrescription = repository.save(userPrescription);

        return userPrescriptionMapper.toUserPrescriptionResponse(userPrescription);
    }

    @Cacheable(
            value = "PRESCRIPTION_LIST_CACHE",
            key = "T(com.fyp.profile_service.utils.ProfileServiceUtil).getCurrentUserId()")
    public List<PrescriptionGeneralResponse> getMyPrescriptions() {
        String userId = ProfileServiceUtil.getCurrentUserId();
        List<UserPrescription> userPrescriptionList =
                repository.findAllByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        log.info("Fetching {} prescriptions from database for user: {}", userPrescriptionList.size(), userId);
        return userPrescriptionList.stream()
                .map(userPrescriptionMapper::toUserPrescriptionGeneralResponse)
                .toList();
    }

    @Cacheable(value = "PRESCRIPTION_DETAIL_CACHE", key = "#prescriptionId")
    public PrescriptionResponse getMyPrescription(String prescriptionId) {
        String userId = ProfileServiceUtil.getCurrentUserId();

        UserPrescription userPrescription = repository
                .findByIdAndUserId(prescriptionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.PRESCRIPTION_NOT_FOUND));
        log.info("Fetching prescription {} from database for user: {}", prescriptionId, userId);
        return userPrescriptionMapper.toUserPrescriptionResponse(userPrescription);
    }

    @Caching(
            evict = {
                @CacheEvict(
                        value = "PRESCRIPTION_LIST_CACHE",
                        key = "T(com.fyp.profile_service.utils.ProfileServiceUtil).getCurrentUserId()"),
                @CacheEvict(value = "PRESCRIPTION_DETAIL_CACHE", key = "#prescriptionId")
            })
    public PrescriptionResponse updateMyPrescription(
            String prescriptionId, PatientPrescriptionUpdateRequest request, List<MultipartFile> newImages) {
        String userId = ProfileServiceUtil.getCurrentUserId();
        UserPrescription prescription = repository
                .findByIdAndUserId(prescriptionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        List<String> finalImageUrls = new ArrayList<>();

        if (prescription.getImageURLS() != null && !prescription.getImageURLS().isEmpty()) {
            List<String> oldImageUrls = prescription.getImageURLS();
            List<String> keptImageUrls = (request.getImageURLS() != null) ? request.getImageURLS() : new ArrayList<>();

            // Delete images that were removed - in parallel
            List<String> urlsToDelete = oldImageUrls.stream()
                    .filter(url -> !keptImageUrls.contains(url))
                    .toList();

            if (!urlsToDelete.isEmpty()) {
                String authToken = ProfileServiceUtil.getAuthorizationToken();

                List<CompletableFuture<Void>> deleteFutures = urlsToDelete.stream()
                        .map(url -> asyncServiceFileManagement.deleteImageAsync(url, authToken))
                        .toList();

                // Wait for all deletions to complete
                CompletableFuture.allOf(deleteFutures.toArray(new CompletableFuture[0]))
                        .join();

                log.info("Deleted {} prescription images for user: {}", urlsToDelete.size(), userId);
            }

            // Add kept images to final list
            finalImageUrls.addAll(keptImageUrls);
        }

        if (newImages != null && !newImages.isEmpty()) {
            // Extract auth token from current request for async propagation
            String authToken = ProfileServiceUtil.getAuthorizationToken();

            // Upload new images in parallel using async service
            List<CompletableFuture<String>> uploadFutures = newImages.stream()
                    .map(image -> asyncServiceFileManagement.uploadImageAsync(image, authToken))
                    .toList();

            // Wait for all uploads to complete
            CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0]))
                    .join();

            // Collect all uploaded URLs and add to final list
            List<String> newUploadedUrls =
                    uploadFutures.stream().map(CompletableFuture::join).toList();

            finalImageUrls.addAll(newUploadedUrls);
            log.info("Uploaded {} new prescription images for user: {}", newUploadedUrls.size(), userId);
        }
        prescription.setImageURLS(finalImageUrls);

        userPrescriptionMapper.updatePrescriptionFromPatient(prescription, request);
        prescription.setUpdatedAt(Instant.now());

        return userPrescriptionMapper.toUserPrescriptionResponse(repository.save(prescription));
    }

    @Caching(
            evict = {
                @CacheEvict(
                        value = "PRESCRIPTION_LIST_CACHE",
                        key = "T(com.fyp.profile_service.utils.ProfileServiceUtil).getCurrentUserId()"),
                @CacheEvict(value = "PRESCRIPTION_DETAIL_CACHE", key = "#prescriptionId")
            })
    public void deleteMyPrescription(String prescriptionId) {
        String userId = ProfileServiceUtil.getCurrentUserId();
        UserPrescription prescription = repository
                .findByIdAndUserId(prescriptionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        // Delete all associated prescription images in parallel
        if (prescription.getImageURLS() != null && !prescription.getImageURLS().isEmpty()) {
            String authToken = ProfileServiceUtil.getAuthorizationToken();

            List<CompletableFuture<Void>> deleteFutures = prescription.getImageURLS().stream()
                    .map(url -> asyncServiceFileManagement.deleteImageAsync(url, authToken))
                    .toList();

            // Wait for all deletions to complete
            CompletableFuture.allOf(deleteFutures.toArray(new CompletableFuture[0]))
                    .join();

            log.info(
                    "Deleted {} prescription images for user: {}",
                    prescription.getImageURLS().size(),
                    userId);
        }

        // Delete the prescription from database
        repository.delete(prescription);
        log.info("Deleted prescription: {} for user: {}", prescriptionId, userId);
    }

    // Doctor Prescription Management
    // Note: NOT cached - contains dynamic access status that varies by doctor
    @PreAuthorize("hasRole('DOCTOR')")
    public List<PrescriptionGeneralResponse> getGeneralPrescription(String patientId) {
        String doctorId = ProfileServiceUtil.getCurrentUserId();
        List<UserPrescription> userPrescription =
                repository.findAllByUserId(patientId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userPrescription.stream()
                .map(prescription -> {
                    PrescriptionGeneralResponse response =
                            userPrescriptionMapper.toUserPrescriptionGeneralResponse(prescription);

                    // Dynamically fetch access status for this specific doctor
                    AccessStatus accessStatus =
                            prescriptionAccessService.getAccessStatusForDoctor(doctorId, prescription.getId());
                    response.setAccessStatus(accessStatus.name());

                    return response;
                })
                .toList();
    }

    @PreAuthorize("hasRole('DOCTOR')")
    public PrescriptionResponse getOnePatientPrescription(String patientId, String prescriptionId) {
        String doctorId = ProfileServiceUtil.getCurrentUserId();
        if (!prescriptionAccessService.hasAccess(doctorId, prescriptionId, AccessStatus.APPROVED)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        UserPrescription userPrescription =
                repository.findById(prescriptionId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userPrescriptionMapper.toUserPrescriptionResponse(userPrescription);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    public PrescriptionAccessResponse requestPrescriptionAccess(
            String prescriptionId, String patientId, PrescriptionNotification.RequestReason requestReason) {
        String doctorId = ProfileServiceUtil.getCurrentUserId();

        UserPrescription userPrescription = repository
                .findById(prescriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.PRESCRIPTION_NOT_FOUND));
        String prescriptionName = userPrescription.getPrescriptionName();

        log.info("Current prescriptionName is {}", userPrescription.getPrescriptionName());
        String reason = requestReason.getRequestReason();

        var response = prescriptionAccessService.createRequest(PrescriptionNotification.builder()
                .prescriptionId(prescriptionId)
                .patientUserId(patientId)
                .prescriptionName(prescriptionName)
                .doctorUserId(doctorId)
                .requestReason(reason)
                .build());

        log.info("Access request created with status: {}", response.getAccessStatus());
        return response;
    }

    @PreAuthorize("hasRole('DOCTOR')")
    public PrescriptionResponse doctorUpdatePrescription(
            String prescriptionId, DoctorPrescriptionUpdateRequest doctorPrescriptionUpdateRequest) {
        String doctorId = ProfileServiceUtil.getCurrentUserId();

        // Only allow update if access is approved
        if (!prescriptionAccessService.hasAccess(doctorId, prescriptionId, AccessStatus.APPROVED)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        UserPrescription userPrescription = repository
                .findById(prescriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        // Use doctor-specific mapper method to ensure only medication fields are updated
        userPrescriptionMapper.updatePrescriptionFromDoctor(userPrescription, doctorPrescriptionUpdateRequest);
        userPrescription = repository.save(userPrescription);

        // Evict caches after update
        cacheEvictionUtil.evictPrescriptionCaches(prescriptionId);

        PrescriptionNotification prescriptionNotification =
                PrescriptionNotification.prescriptionUpdate(userPrescription);
        notificationService.sendRealtimeNotification(prescriptionNotification);
        return userPrescriptionMapper.toUserPrescriptionResponse(userPrescription);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    public void doctorDeletePrescription(String prescriptionId) {
        String doctorId = ProfileServiceUtil.getCurrentUserId();

        // Only allow delete if access is approved
        if (!prescriptionAccessService.hasAccess(doctorId, prescriptionId, AccessStatus.APPROVED)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        UserPrescription userPrescription = repository
                .findById(prescriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        // Get patient ID for authorized deletion
        String patientId = userPrescription.getUserId();

        // Delete all associated prescription images in parallel using authorized deletion
        if (userPrescription.getImageURLS() != null
                && !userPrescription.getImageURLS().isEmpty()) {
            String authToken = ProfileServiceUtil.getAuthorizationToken();

            List<CompletableFuture<Void>> deleteFutures = userPrescription.getImageURLS().stream()
                    .map(url -> asyncServiceFileManagement.authorizedUserDeleteImageAsync(url, patientId, authToken))
                    .toList();

            // Wait for all deletions to complete
            CompletableFuture.allOf(deleteFutures.toArray(new CompletableFuture[0]))
                    .join();

            log.info(
                    "Doctor {} deleted {} prescription images for prescription: {} owned by patient: {}",
                    doctorId,
                    userPrescription.getImageURLS().size(),
                    prescriptionId,
                    patientId);
        }

        // Evict caches using the patientId we already have (avoids extra DB fetch)
        cacheEvictionUtil.evictPrescriptionCaches(patientId, prescriptionId);

        // Delete the prescription from database
        repository.deleteById(prescriptionId);

        PrescriptionNotification prescriptionNotification =
                PrescriptionNotification.prescriptionDelete(userPrescription);

        notificationService.sendRealtimeNotification(prescriptionNotification);
    }
}
