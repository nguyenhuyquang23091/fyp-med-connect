package com.fyp.authservice.mapper;

import com.fyp.authservice.dto.request.PermissionRequest;
import com.fyp.authservice.dto.request.UserCreationRequest;
import com.fyp.authservice.dto.request.UserUpdateRequest;
import com.fyp.authservice.dto.response.PermissionResponse;
import com.fyp.authservice.dto.response.UserResponse;
import com.fyp.authservice.entity.Permission;
import com.fyp.authservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
