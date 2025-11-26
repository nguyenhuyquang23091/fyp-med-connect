package com.fyp.profile_service.controller;

import java.util.List;

import com.fyp.profile_service.dto.response.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fyp.profile_service.dto.request.AdminUpdateUserProfileRequest;
import com.fyp.profile_service.dto.request.ApiResponse;
import com.fyp.profile_service.dto.request.ProfileUpdateRequest;
import com.fyp.profile_service.dto.response.UserProfileResponse;
import com.fyp.profile_service.service.UserProfileService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class UserProfileController {
    UserProfileService userProfileService;
    // admin api
    @GetMapping("/users/{userId}")
    public ApiResponse<UserProfileResponse> getAllProfile(
            @PathVariable @NotBlank(message = "UserId ID cannot be blank") String userId) {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.getOneUserProfile(userId))
                .build();
    }

    @GetMapping("/users")
    public ApiResponse<PageResponse<UserProfileResponse>> getAllProfile(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size
    ) {
        return ApiResponse.<PageResponse<UserProfileResponse>>builder()
                .result(userProfileService.getAllUserProfile(page, size))
                .build();
    }

    @DeleteMapping("/users/{profileId}")
    public void deleteUser(@PathVariable @NotBlank(message = "Profile ID cannot be blank") String profileId) {
        userProfileService.deleteProfile(profileId);
    }

    @PatchMapping("/users/{userId}")
    public ApiResponse<UserProfileResponse> adminUpdateProfile(
            @PathVariable @Valid String userId, @RequestBody AdminUpdateUserProfileRequest request) {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.adminUpdateOneUserProfile(userId, request))
                .build();
    }

    // doctor api
    @GetMapping("/users/get-all-patients")
    public ApiResponse<PageResponse<UserProfileResponse>> getAllPatientProfile(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size
    ) {
       return ApiResponse.<PageResponse<UserProfileResponse>>builder()
               .result(userProfileService.getAllPatientProfile(page, size))
               .build();
    }
    // user api
    @PutMapping("/users/my-profile")
    public ApiResponse<UserProfileResponse> updateProfile(
            @Valid @RequestBody ProfileUpdateRequest profileUpdateRequest) {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.updateUserProfile(profileUpdateRequest))
                .build();
    }

    @GetMapping("/users/my-profile")
    public ApiResponse<UserProfileResponse> getMyProfile() {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.getMyUserProfile())
                .build();
    }

    @PutMapping("/users/avatar")
    public ApiResponse<UserProfileResponse> updateAvatar(@RequestParam("file") MultipartFile file) {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.updateUserAvatar(file))
                .build();
    }

    @DeleteMapping("/users/avatar")
    public ApiResponse<UserProfileResponse> deleteAvatar() {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.deleteUserAvatar())
                .build();
    }
}
