package com.fyp.appointment_service.repository.httpCLient;

import com.fyp.appointment_service.configuration.AuthenticationInterceptor;
import com.fyp.appointment_service.dto.request.ApiResponse;
import com.fyp.appointment_service.dto.response.DoctorProfileResponse;
import com.fyp.appointment_service.dto.response.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;


@FeignClient(
        name = "profile-service",
        url = "${microservices.profile-service.url}",
        configuration = {AuthenticationInterceptor.class})
public interface ProfileClient {

    @GetMapping(value = "/profile/doctorProfile/allDoctors")
    ApiResponse<List<DoctorProfileResponse>> getAllAvailableDoctor();

    @GetMapping(value = "/profile/doctorProfile/getOneDoctorProfile/{doctorId}")
    ApiResponse<DoctorProfileResponse> getOneDoctorProfile(@PathVariable String doctorId);

    @GetMapping(value = "/profile/users/my-profile")
   ApiResponse<UserProfileResponse> getMyProfile();
}






