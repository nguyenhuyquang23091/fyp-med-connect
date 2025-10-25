package com.fyp.authservice.service;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fyp.authservice.constant.PredefinedRole;
import com.fyp.authservice.dto.request.AdminUpdateRequest;
import com.fyp.authservice.entity.Role;
import com.fyp.authservice.mapper.ProfileMapper;
import com.fyp.authservice.mapper.RoleMapper;
import com.fyp.authservice.repository.RoleRepository;
import com.fyp.authservice.repository.http_client.ProfileClient;
import com.fyp.event.dto.NotificationEvent;
import com.fyp.event.dto.UserRoleUpdateEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fyp.authservice.dto.request.UserCreationRequest;
import com.fyp.authservice.dto.request.UserUpdateRequest;
import com.fyp.authservice.dto.response.UserResponse;
import com.fyp.authservice.entity.User;
import com.fyp.authservice.exceptions.AppException;
import com.fyp.authservice.exceptions.ErrorCode;
import com.fyp.authservice.mapper.UserMapper;
import com.fyp.authservice.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import javax.swing.plaf.nimbus.NimbusStyle;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    private final RoleMapper roleMapper;
    UserRepository userRepository;
     RoleRepository roleRepository;
     ProfileClient profileClient;
      UserMapper userMapper;
      PasswordEncoder passwordEncoder;
      ProfileMapper profileMapper;
      KafkaTemplate<String, Object> kafkaTemplate;


    public UserResponse createUser(UserCreationRequest request)  {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.USER_EXISTED);
        User user  = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.PATIENT_ROLE).ifPresent(roles::add);
        user.setRoles(roles);
        user = userRepository.save(user);

        var userProfileRequest = profileMapper.toProfileCreationRequest(request);
        userProfileRequest.setUserId(user.getId());
        userProfileRequest.setEmail(user.getEmail());
        userProfileRequest.setGender(request.getGender());
        profileClient.createProfile(userProfileRequest);
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("EMAIL")
                .recipient(request.getEmail())
                .subject("Welcome to Med Connect")
                .body("Hello, " + request.getUsername())
                .build();
        //publish message to kafka
        kafkaTemplate.send("notification-delivery",  notificationEvent);
        return  userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getUsers(){
        log.info("In method get users: ");
        return userRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getOneUser(String id){
        log.info("In method get one user ");
        return userMapper.toUserResponse( userRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    public UserResponse getMyInfo(){
         //when a request is confirmed, their information is stored in SecurityContextHolder
       var context = SecurityContextHolder.getContext();
       String id = context.getAuthentication().getName();

      User user = userRepository.findById(id).
              orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

    return userMapper.toUserResponse(user);

    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse adminUpdateUser(String id, AdminUpdateRequest updateRequest ){
        User user = userRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)) ;
        Set<String> oldRoles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        userMapper.adminUpdateUser(user, updateRequest);
        user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        var roles = roleRepository.findAllById(updateRequest.getRoles());
        user.setRoles(new HashSet<>(roles));

        User savedUser = userRepository.save(user);
        if(!oldRoles.equals(roles)){
            Set<String> newRoles = savedUser.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
            UserRoleUpdateEvent userRoleUpdateEvent =
                    UserRoleUpdateEvent.builder()
                            .userId(savedUser.getId())
                            .oldRoles(oldRoles)
                            .newRoles(newRoles)
                            .email(savedUser.getEmail())
                            .build();
            kafkaTemplate.send("user-role-updated", userRoleUpdateEvent);
            log.info("Published role change event for user {} - Old roles: {}, New roles: {}",
                    savedUser.getId(), oldRoles, newRoles);
        }

        return userMapper.toUserResponse(savedUser);
    }


    public UserResponse updateUser( UserUpdateRequest updateRequest ){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String id = authentication.getName();
        log.info("Id{}", id);
        User user = userRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)) ;
        userMapper.updateUser(user, updateRequest);
        user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public List<String> findUserIdByRoles (String role){
        return userRepository.findUserIdByRole(role).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String id){
        userRepository.deleteById(id);
    }

}
