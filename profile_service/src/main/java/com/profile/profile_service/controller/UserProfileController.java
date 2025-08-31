package com.profile.profile_service.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.profile.profile_service.dto.request.ApiResponse;
import com.profile.profile_service.dto.request.ProfileUpdateRequest;
import com.profile.profile_service.dto.response.UserProfileResponse;
import com.profile.profile_service.entity.UserProfile;
import com.profile.profile_service.service.UserProfileService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserProfileController {
    UserProfileService userProfileService;
    // admin api
    @GetMapping("/users/{profileId}")
    public UserProfileResponse getProfile(@PathVariable String profileId) {
        return userProfileService.getOneUserProfile(profileId);
    }

    @GetMapping("/users")
    public List<UserProfile> getProfile() {
        return userProfileService.getAllUserProfile();
    }

    @DeleteMapping("/users/{profileId}")
    public void delteUser(@PathVariable String profileId) {
        userProfileService.deleteProfile(profileId);
    }
    // user api
    @PutMapping("/users/my-profile")
    public ApiResponse<UserProfileResponse> updateProfile(@RequestBody ProfileUpdateRequest profileUpdateRequest) {
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
    public ApiResponse<UserProfileResponse> updateAvatar(
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.updateUserAvatar(file))
                .build();
    }
}
