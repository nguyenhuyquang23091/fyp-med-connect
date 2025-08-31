package com.fyp.authservice.service;


import com.fyp.authservice.dto.request.RoleRequest;
import com.fyp.authservice.dto.response.RoleResponse;
import com.fyp.authservice.mapper.RoleMapper;
import com.fyp.authservice.repository.PermissionRepository;
import com.fyp.authservice.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService  {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse create(RoleRequest request){
      var role = roleMapper.toRole(request);
      var permissions = permissionRepository.findAllById(request.getPermissions());
      // the purpose of using hashset in here is to avoid duplicate,
        // for the uniqueness of each permission
      role.setPermissions(new HashSet<>(permissions));
      role = roleRepository.save(role);

      return  roleMapper.toRoleResponse(role);
    }
    @PreAuthorize("hasRole('ADMIN')")
    public List<RoleResponse> getAll(){
        var roles = roleRepository.findAll();
        return roles.stream().map(roleMapper::toRoleResponse).toList();
    }
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(String role){
        roleRepository.deleteById(role);

    }
}
