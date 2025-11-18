package com.fyp.profile_service.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fyp.profile_service.dto.request.ApiResponse;
import com.fyp.profile_service.dto.request.ProfileUpdateRequest;
import com.fyp.profile_service.dto.response.UserProfileResponse;
import com.fyp.profile_service.entity.UserProfile;
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
    public ApiResponse<UserProfileResponse> getProfile(
            @PathVariable @NotBlank(message = "UserId ID cannot be blank") String userId) {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.getOneUserProfile(userId))
                .build();
    }

    @GetMapping("/users")
    public ApiResponse<List<UserProfile>> getProfile() {
        return ApiResponse.<List<UserProfile>>builder()
                .result(userProfileService.getAllUserProfile())
                .build();
    }

    @DeleteMapping("/users/{profileId}")
    public void deleteUser(@PathVariable @NotBlank(message = "Profile ID cannot be blank") String profileId) {
        userProfileService.deleteProfile(profileId);
    }

    // doctor api
    @GetMapping("/users/get-all-patients")
    public ApiResponse<List<UserProfileResponse>> getAllPatientProfile() {
        return ApiResponse.<List<UserProfileResponse>>builder()
                .result(userProfileService.getAllPatientProfile())
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
