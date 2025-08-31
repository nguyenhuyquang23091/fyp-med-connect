package com.fyp.authservice.mapper;

import com.fyp.authservice.dto.request.AdminUpdateRequest;
import com.fyp.authservice.dto.request.UserCreationRequest;
import com.fyp.authservice.dto.request.UserUpdateRequest;
import com.fyp.authservice.dto.response.UserResponse;
import com.fyp.authservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);
    UserResponse toUserResponse(User user);
    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget  User user, UserUpdateRequest userUpdateRequest);
    @Mapping(target = "roles", ignore = true)
    void adminUpdateUser(@MappingTarget  User user, AdminUpdateRequest adminUpdateRequest);


}
