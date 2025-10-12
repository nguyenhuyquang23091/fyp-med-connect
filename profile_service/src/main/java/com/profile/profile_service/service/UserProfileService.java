package com.profile.profile_service.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.profile.profile_service.dto.request.ProfileCreationRequest;
import com.profile.profile_service.dto.request.ProfileUpdateRequest;
import com.profile.profile_service.dto.response.UserProfileResponse;
import com.profile.profile_service.entity.UserProfile;
import com.profile.profile_service.exceptions.AppException;
import com.profile.profile_service.exceptions.ErrorCode;
import com.profile.profile_service.mapper.UserProfileMapper;
import com.profile.profile_service.repository.UserProfileRepository;
import com.profile.profile_service.repository.httpClient.AuthServiceClient;
import com.profile.profile_service.repository.httpClient.FileFeignClient;
import com.profile.profile_service.utils.ProfileServiceUtil;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileService {
    UserProfileRepository userProfileRepository;
    UserProfileMapper userProfileMapper;
    FileFeignClient fileService;
    AuthServiceClient authServiceClient;

    static final String PATIENT_ROLE = "PATIENT";
    static final String DOCTOR_ROLE = "DOCTOR";

    /**
     * Creates a new user profile.
     *
     * @param request the profile creation request
     * @return UserProfileResponse containing the created profile
     */
    @CachePut(value = "PROFILE_CACHE", key = "#result.userId")
    public UserProfileResponse createUserProfile(ProfileCreationRequest request) {
        UserProfile userProfile = userProfileMapper.toUserProfile(request);

        userProfile = userProfileRepository.save(userProfile);
        log.info(
                "Created User Profile with User id : {} and email : {}",
                userProfile.getUserId(),
                userProfile.getEmail());
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    @CachePut(
            value = "PROFILE_CACHE",
            key = "T(com.profile.profile_service.utils.ProfileServiceUtil).getCurrentUserId()")
    public UserProfileResponse updateUserProfile(ProfileUpdateRequest request) {
        String userId = ProfileServiceUtil.getCurrentUserId();
        log.info("Userid is {}", userId);

        var userProfile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        userProfileMapper.updateProfile(userProfile, request);
        return userProfileMapper.toUserProfileResponse(userProfileRepository.save(userProfile));
    }

    @Cacheable(
            value = "PROFILE_CACHE",
            key = "T(com.profile.profile_service.utils.ProfileServiceUtil).getCurrentUserId()")
    public UserProfileResponse getMyUserProfile() {
        String userId = ProfileServiceUtil.getCurrentUserId();
        var userProfile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    @CachePut(
            value = "PROFILE_CACHE",
            key = "T(com.profile.profile_service.utils.ProfileServiceUtil).getCurrentUserId()")
    public UserProfileResponse updateUserAvatar(MultipartFile file) {
        String userId = ProfileServiceUtil.getCurrentUserId();
        var userProfile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String oldAvatarUrl = userProfile.getAvatar();
        if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
            try {
                fileService.deleteMedia(oldAvatarUrl);
                log.info("Deleted old avatar: {}", oldAvatarUrl);
            } catch (Exception e) {
                log.warn("Failed to delete old avatar, continuing with upload", e);
            }
        }

        // Upload new avatar
        var response = fileService.uploadMedia(file);
        userProfile.setAvatar(response.getResult().getUrl());

        userProfile = userProfileRepository.save(userProfile);
        log.info("Current avatar url is {}", userProfile.getAvatar());
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    @CachePut(
            value = "PROFILE_CACHE",
            key = "T(com.profile.profile_service.utils.ProfileServiceUtil).getCurrentUserId()")
    public UserProfileResponse deleteUserAvatar() {
        String userId = ProfileServiceUtil.getCurrentUserId();
        var userProfile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String avatarUrl = userProfile.getAvatar();
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            throw new AppException(ErrorCode.AVATAR_NOT_FOUND);
        }

        // Delete from file service
        fileService.deleteMedia(avatarUrl);
        log.info("Deleted avatar from file service: {}", avatarUrl);

        // Update profile to remove avatar
        userProfile.setAvatar(null);
        userProfile = userProfileRepository.save(userProfile);

        return userProfileMapper.toUserProfileResponse(userProfile);
    }
    // DOCTOR

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('VIEW_PATIENT_RECORDS')")
    public List<UserProfileResponse> getAllPatientProfile() {
        log.info("Fetching all patient profiles via cross-service call");

        // Step 1: Get all user IDs with PATIENT role from AuthService
        List<String> patientUserIds =
                authServiceClient.getUserIdsByRole(PATIENT_ROLE).getResult();

        if (patientUserIds == null || patientUserIds.isEmpty()) {
            log.warn("No patients found in AuthService");
            return List.of();
        }

        log.info("Found {} patients from AuthService", patientUserIds.size());

        // Step 2: Fetch profiles by user IDs from ProfileService
        List<UserProfile> patientProfileList = userProfileRepository.findAllByUserIdIn(patientUserIds);

        return patientProfileList.stream()
                .map(userProfileMapper::toUserProfileResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('VIEW_DOCTOR_PROFILES')")
    public List<UserProfileResponse> getAllDoctorProfile() {
        log.info("Fetching all doctor profiles via cross-service call");

        // Step 1: Get all user IDs with DOCTOR role from AuthService
        List<String> doctorUserIds =
                authServiceClient.getUserIdsByRole(DOCTOR_ROLE).getResult();

        if (doctorUserIds == null || doctorUserIds.isEmpty()) {
            log.warn("No doctors found in AuthService");
            return List.of();
        }

        log.info("Found {} doctors from AuthService", doctorUserIds.size());

        // Step 2: Fetch profiles by user IDs from ProfileService
        List<UserProfile> doctorProfileList = userProfileRepository.findAllByUserIdIn(doctorUserIds);

        return doctorProfileList.stream()
                .map(userProfileMapper::toUserProfileResponse)
                .toList();
    }

    // ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserProfile> getAllUserProfile() {
        return userProfileRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('VIEW_PATIENT_RECORDS')")
    public UserProfileResponse getOneUserProfile(String userId) {
        UserProfile userProfile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(cacheNames = "PROFILE_CACHE", key = "#id")
    public void deleteProfile(String id) {
        userProfileRepository.deleteById(id);
    }
}
