package com.fyp.authservice.mapper;

import com.fyp.authservice.dto.request.ProfileCreationRequest;
import com.fyp.authservice.dto.request.UserCreationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileCreationRequest toProfileCreationRequest(UserCreationRequest request);
}
