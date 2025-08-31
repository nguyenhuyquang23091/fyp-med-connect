package com.fyp.authservice.controller;


import com.fyp.authservice.dto.request.AdminUpdateRequest;
import com.fyp.authservice.dto.request.ApiResponse;
import com.fyp.authservice.dto.request.UserCreationRequest;
import com.fyp.authservice.dto.request.UserUpdateRequest;
import com.fyp.authservice.dto.response.UserResponse;
import com.fyp.authservice.entity.User;
import com.fyp.authservice.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {
    //auto defined final because of field defaults annotations
     UserService userService;
    @PostMapping("/registration")
    ApiResponse<UserResponse> createUser(@RequestBody @Valid  UserCreationRequest request){
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }
    @GetMapping
    ApiResponse<List<User>> getUsers(){
       var authentication =  SecurityContextHolder.getContext().getAuthentication();
       log.info("Email : {}", authentication.getName());
        authentication.getAuthorities()
                .forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));
       return ApiResponse.<List<User>>builder()

               .result(userService.getUsers())
               .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable("userId") String userId){
        return ApiResponse.<UserResponse>builder().result(userService.getOneUser(userId)).build();
    }

   @GetMapping("/my-info")
   ApiResponse<UserResponse> getMyInfo(){
        return ApiResponse.<UserResponse>builder().
                result(userService.getMyInfo()).build();
   }

    @PutMapping("/my-info")
    UserResponse updateUser(@RequestBody UserUpdateRequest updateRequest){
       return userService.updateUser( updateRequest);
    }
    @PutMapping("/{userId}")
    UserResponse adminUpdateUser( @PathVariable String userId, @RequestBody AdminUpdateRequest updateRequest){
        return userService.adminUpdateUser(userId, updateRequest);
    }

    @DeleteMapping("/{userId}")
    void deleteUser(@PathVariable String userId){
        userService.deleteUser(userId);
    }

}
