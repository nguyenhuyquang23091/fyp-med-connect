package com.fyp.authservice.mapper;

import com.fyp.authservice.dto.request.AdminUpdateRequest;
import com.fyp.authservice.dto.request.RoleRequest;
import com.fyp.authservice.dto.request.RoleUpdateRequest;
import com.fyp.authservice.dto.response.RoleResponse;
import com.fyp.authservice.entity.Role;
import com.fyp.authservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target =  "permissions", ignore = true)
    Role toRole(RoleRequest request);
    RoleResponse toRoleResponse(Role role);


    @Mapping(target =  "permissions", ignore = true)
    void roleUpdateMapper(@MappingTarget Role role, RoleUpdateRequest roleUpdateRequest);

}
