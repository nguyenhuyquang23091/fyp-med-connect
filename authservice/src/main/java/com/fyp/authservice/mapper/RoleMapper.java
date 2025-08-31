package com.fyp.authservice.mapper;

import com.fyp.authservice.dto.request.RoleRequest;
import com.fyp.authservice.dto.response.RoleResponse;
import com.fyp.authservice.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target =  "permissions", ignore = true)
    Role toRole(RoleRequest request);
    RoleResponse toRoleResponse(Role role);
}
