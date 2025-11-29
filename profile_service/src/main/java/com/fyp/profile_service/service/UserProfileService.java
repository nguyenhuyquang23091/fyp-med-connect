package com.fyp.profile_service.service;

import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fyp.profile_service.dto.request.AdminUpdateUserProfileRequest;
import com.fyp.profile_service.dto.request.ProfileCreationRequest;
import com.fyp.profile_service.dto.request.ProfileUpdateRequest;
import com.fyp.profile_service.dto.response.PageResponse;
import com.fyp.profile_service.dto.response.UserProfileResponse;
import com.fyp.profile_service.entity.UserProfile;
import com.fyp.profile_service.exceptions.AppException;
import com.fyp.profile_service.exceptions.ErrorCode;
import com.fyp.profile_service.mapper.UserProfileMapper;
import com.fyp.profile_service.repository.UserProfileRepository;
import com.fyp.profile_service.repository.httpClient.AuthServiceClient;
import com.fyp.profile_service.repository.httpClient.FileFeignClient;
import com.fyp.profile_service.utils.ProfileServiceUtil;

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

    @CachePut(value = "PROFILE_CACHE", key = "#result.userId")
    public UserProfileResponse createUserProfile(ProfileCreationRequest request) {
        UserProfile userProfile = userProfileMapper.toUserProfile(request);

        userProfile = userProfileRepository.save(userProfile);
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    @Caching(
            put = {
                @CachePut(
                        value = "PROFILE_CACHE",
                        key = "T(com.fyp.profile_service.utils.ProfileServiceUtil).getCurrentUserId()")
            },
            evict = {
                @CacheEvict(
                        value = "DOCTOR_PROFILE_CACHE",
                        key = "T(com.fyp.profile_service.utils.ProfileServiceUtil).getCurrentUserId()")
            })
    public UserProfileResponse updateUserProfile(ProfileUpdateRequest request) {
        String userId = ProfileServiceUtil.getCurrentUserId();
        log.info("Userid is {}", userId);
        var userProfile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        userProfileMapper.updateProfile(userProfile, request);
        return userProfileMapper.toUserProfileResponse(userProfileRepository.save(userProfile));
    }

    @Cacheable(value = "PROFILE_CACHE", key = "T(com.fyp.profile_service.utils.ProfileServiceUtil).getCurrentUserId()")
    public UserProfileResponse getMyUserProfile() {
        String userId = ProfileServiceUtil.getCurrentUserId();
        var userProfile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    @Caching(
            put = {
                @CachePut(
                        value = "PROFILE_CACHE",
                        key = "T(com.fyp.profile_service.utils.ProfileServiceUtil).getCurrentUserId()")
            },
            evict = {
                @CacheEvict(
                        value = "DOCTOR_PROFILE_CACHE",
                        key = "T(com.fyp.profile_service.utils.ProfileServiceUtil).getCurrentUserId()")
            })
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

    @Caching(
            put = {
                @CachePut(
                        value = "PROFILE_CACHE",
                        key = "T(com.fyp.profile_service.utils.ProfileServiceUtil).getCurrentUserId()")
            },
            evict = {
                @CacheEvict(
                        value = "DOCTOR_PROFILE_CACHE",
                        key = "T(com.fyp.profile_service.utils.ProfileServiceUtil).getCurrentUserId()")
            })
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
    public PageResponse<UserProfileResponse> getAllPatientProfile(int page, int size) {
        log.info("Fetching all patient profiles via cross-service call");

        // Step 1: Get all user IDs with PATIENT role from AuthService
        Set<String> patientUserIds =
                Set.copyOf(authServiceClient.getUserIdsByRole(PATIENT_ROLE).getResult());

        if (patientUserIds == null || patientUserIds.isEmpty()) {
            log.warn("No patients found in AuthService");
        }

        log.info("Found {} patients from AuthService", patientUserIds.size());

        Pageable pageable = PageRequest.of(page - 1, size);

        var allPatientProfile = userProfileRepository.findAllByUserIdIn(patientUserIds, pageable);

        return PageResponse.<UserProfileResponse>builder()
                .currentPage(page)
                .pageSize(allPatientProfile.getSize())
                .totalPages(allPatientProfile.getTotalPages())
                .totalElements(allPatientProfile.getTotalElements())
                .data(allPatientProfile.getContent().stream()
                        .map(userProfileMapper::toUserProfileResponse)
                        .toList())
                .build();
    }

    // ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<UserProfileResponse> getAllUserProfile(int page, int size) {

        Pageable pageable = PageRequest.of(page - 1, size);

        var allUserProfiles = userProfileRepository.findAllBy(pageable);

        return PageResponse.<UserProfileResponse>builder()
                .currentPage(page)
                .pageSize(allUserProfiles.getSize())
                .totalPages(allUserProfiles.getTotalPages())
                .totalElements(allUserProfiles.getTotalElements())
                .data(allUserProfiles.getContent().stream()
                        .map(userProfileMapper::toUserProfileResponse)
                        .toList())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('VIEW_PATIENT_RECORDS')")
    public UserProfileResponse getOneUserProfile(String userId) {
        UserProfile userProfile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Caching(
            evict = {
                @CacheEvict(cacheNames = "PROFILE_CACHE", key = "#id"),
                @CacheEvict(cacheNames = "DOCTOR_PROFILE_CACHE", key = "#id")
            })
    public void deleteProfile(String id) {
        userProfileRepository.deleteById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Caching(
            evict = {
                @CacheEvict(cacheNames = "PROFILE_CACHE", key = "#userId"),
                @CacheEvict(cacheNames = "DOCTOR_PROFILE_CACHE", key = "#userId")
            })
    public UserProfileResponse adminUpdateOneUserProfile(String userId, AdminUpdateUserProfileRequest request) {
        UserProfile userProfile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userProfileMapper.adminUpdateProfile(userProfile, request);

        userProfileRepository.save(userProfile);

        return userProfileMapper.toUserProfileResponse(userProfile);
    }
}
