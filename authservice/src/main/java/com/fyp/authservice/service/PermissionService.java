package com.fyp.authservice.service;


import com.fyp.authservice.dto.request.PermissionRequest;
import com.fyp.authservice.dto.response.PermissionResponse;
import com.fyp.authservice.entity.Permission;
import com.fyp.authservice.mapper.PermissionMapper;
import com.fyp.authservice.repository.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public PermissionResponse create(PermissionRequest request){
        Permission permission = permissionMapper.toPermission(request);
        permission = permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }
    @PreAuthorize("hasRole('ADMIN')")
    public List<PermissionResponse> getAll(){
        var permissions = permissionRepository.findAll();
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }
    @PreAuthorize("hasRole('ADMIN')")
    public void deletePermission(String permission){
        permissionRepository.deleteById(permission);
    }
}
