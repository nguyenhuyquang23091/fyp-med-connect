package com.fyp.profile_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.fyp.profile_service.dto.request.ProfileCreationRequest;
import com.fyp.profile_service.dto.request.ProfileUpdateRequest;
import com.fyp.profile_service.dto.response.UserProfileResponse;
import com.fyp.profile_service.entity.UserProfile;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    UserProfile toUserProfile(ProfileCreationRequest request);

    UserProfileResponse toUserProfileResponse(UserProfile userProfile);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "email", ignore = true)
    void updateProfile(@MappingTarget UserProfile userProfile, ProfileUpdateRequest profileUpdateRequest);
}
