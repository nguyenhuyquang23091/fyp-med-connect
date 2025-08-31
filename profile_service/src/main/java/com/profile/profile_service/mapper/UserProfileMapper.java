package com.profile.profile_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.profile.profile_service.dto.request.ProfileCreationRequest;
import com.profile.profile_service.dto.request.ProfileUpdateRequest;
import com.profile.profile_service.dto.response.UserProfileResponse;
import com.profile.profile_service.entity.UserProfile;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    UserProfile toUserProfile(ProfileCreationRequest request);

    UserProfileResponse toUserProfileResponse(UserProfile userProfile);

    void updateProfile(@MappingTarget UserProfile userProfile, ProfileUpdateRequest profileUpdateRequest);
}
