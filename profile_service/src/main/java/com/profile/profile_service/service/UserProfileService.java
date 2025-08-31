package com.profile.profile_service.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.profile.profile_service.repository.httpClient.FileFeignClient;

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

    public UserProfileResponse createUserProfile(ProfileCreationRequest request) {
        UserProfile userProfile = userProfileMapper.toUserProfile(request);
        userProfile = userProfileRepository.save(userProfile);
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    public UserProfileResponse updateUserProfile(ProfileUpdateRequest request) {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        log.info("Userid is {}", userId);

        var userProfile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        userProfileMapper.updateProfile(userProfile, request);
        return userProfileMapper.toUserProfileResponse(userProfileRepository.save(userProfile));
    }

    public UserProfileResponse getMyUserProfile() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        var userProfile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    public UserProfileResponse updateUserAvatar(MultipartFile file) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        var userProfile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        var response = fileService.uploadMedia(file);

        userProfile.setAvatar(response.getResult().getUrl());
        // upload file call API from file service

        return userProfileMapper.toUserProfileResponse(userProfileRepository.save(userProfile));
    }
    // ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserProfile> getAllUserProfile() {
        return userProfileRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserProfileResponse getOneUserProfile(String id) {
        UserProfile userProfile =
                userProfileRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProfile(String id) {
        userProfileRepository.deleteById(id);
    }
}
