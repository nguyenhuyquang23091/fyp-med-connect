package com.profile.profile_service.controller;

import org.springframework.web.bind.annotation.*;

import com.profile.profile_service.dto.request.ProfileCreationRequest;
import com.profile.profile_service.dto.response.UserProfileResponse;
import com.profile.profile_service.service.UserProfileService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InternalUserProfileController {
    UserProfileService userProfileService;

    @PostMapping("/internal/users")
    public UserProfileResponse creationProfile(@RequestBody ProfileCreationRequest request) {
        return userProfileService.createUserProfile(request);
    }
}
