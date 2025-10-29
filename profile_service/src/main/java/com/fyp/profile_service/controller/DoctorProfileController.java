package com.fyp.profile_service.controller;

import java.util.List;

import com.fyp.profile_service.dto.request.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.fyp.profile_service.dto.response.DoctorExperienceResponse;
import com.fyp.profile_service.dto.response.DoctorProfileResponse;
import com.fyp.profile_service.dto.response.DoctorServiceResponse;
import com.fyp.profile_service.dto.response.DoctorSpecialtyResponse;
import com.fyp.profile_service.service.DoctorProfileService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/doctorProfile")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class DoctorProfileController {

    DoctorProfileService doctorProfileService;

    @PutMapping("/baseProfile")
    public ApiResponse<DoctorProfileResponse> updateMyBaseDoctorProfile(@Valid @RequestBody DoctorProfileUpdateRequest doctorProfileUpdateRequest){
        return ApiResponse.<DoctorProfileResponse>builder().result(doctorProfileService.updateBaseDoctorProfile(doctorProfileUpdateRequest)).build();
    }

    @GetMapping("/services")
    public ApiResponse<List<DoctorServiceResponse>> getMyServices() {
        return ApiResponse.<List<DoctorServiceResponse>>builder()
                .result(doctorProfileService.getMyServices())
                .build();
    }

    @PostMapping("/services")
    public ApiResponse<DoctorServiceResponse> addServiceToProfile(@Valid @RequestBody DoctorServiceRequest request) {
        return ApiResponse.<DoctorServiceResponse>builder()
                .result(doctorProfileService.addServiceToProfile(request))
                .build();
    }

    @PutMapping("/services/{relationshipId}")
    public ApiResponse<DoctorServiceResponse> updateServiceInProfile(
            @PathVariable @NotNull(message = "Relationship ID cannot be null") Long relationshipId,
            @Valid @RequestBody DoctorServiceRequest request) {
        return ApiResponse.<DoctorServiceResponse>builder()
                .result(doctorProfileService.updateServiceInProfile(relationshipId, request))
                .build();
    }

    @DeleteMapping("/services/{relationshipId}")
    public ApiResponse<Void> removeServiceFromProfile(
            @PathVariable @NotNull(message = "Relationship ID cannot be null") Long relationshipId) {
        doctorProfileService.removeServiceFromProfile(relationshipId);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/specialties")
    public ApiResponse<List<DoctorSpecialtyResponse>> getMySpecialties() {
        return ApiResponse.<List<DoctorSpecialtyResponse>>builder()
                .result(doctorProfileService.getMySpecialties())
                .build();
    }

    @PostMapping("/specialties")
    public ApiResponse<DoctorSpecialtyResponse> addSpecialtyToProfile(
            @Valid @RequestBody DoctorSpecialtyRequest request) {
        return ApiResponse.<DoctorSpecialtyResponse>builder()
                .result(doctorProfileService.addSpecialtyToProfile(request))
                .build();
    }

    @PutMapping("/specialties/{relationshipId}")
    public ApiResponse<DoctorSpecialtyResponse> updateSpecialtyInProfile(
            @PathVariable @NotNull(message = "Relationship ID cannot be null") Long relationshipId,
            @Valid @RequestBody DoctorSpecialtyRequest request) {
        return ApiResponse.<DoctorSpecialtyResponse>builder()
                .result(doctorProfileService.updateSpecialtyInProfile(relationshipId, request))
                .build();
    }

    @DeleteMapping("/specialties/{relationshipId}")
    public ApiResponse<Void> removeSpecialtyFromProfile(
            @PathVariable @NotNull(message = "Relationship ID cannot be null") Long relationshipId) {
        doctorProfileService.removeSpecialtyFromProfile(relationshipId);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/experiences")
    public ApiResponse<List<DoctorExperienceResponse>> getMyExperiences() {
        return ApiResponse.<List<DoctorExperienceResponse>>builder()
                .result(doctorProfileService.getMyExperiences())
                .build();
    }

    @PatchMapping("/experiences/{experienceId}/metadata")
    public ApiResponse<DoctorExperienceResponse> updateExperienceMetadata(
            @PathVariable @NotBlank(message = "Experience ID cannot be blank") String experienceId,
            @Valid @RequestBody DoctorExperienceUpdateRequest request) {
        return ApiResponse.<DoctorExperienceResponse>builder()
                .result(doctorProfileService.updateExperienceMetadata(experienceId, request))
                .build();
    }

    @GetMapping("/myDoctorProfile")
    public ApiResponse<DoctorProfileResponse> getMyDoctorProfileResponse() {
        return ApiResponse.<DoctorProfileResponse>builder()
                .result(doctorProfileService.getMyDoctorProfile())
                .build();
    }

    @GetMapping("/allDoctors")
    public ApiResponse<List<DoctorProfileResponse>> getAllDoctors() {
        return ApiResponse.<List<DoctorProfileResponse>>builder()
                .result(doctorProfileService.getAllDoctorProfile())
                .build();
    }

    @GetMapping("/getOneDoctorProfile/{doctorId}")
    public ApiResponse<DoctorProfileResponse> getOneDoctor(@PathVariable String doctorId) {
        return ApiResponse.<DoctorProfileResponse>builder()
                .result(doctorProfileService.getOneDoctorProfile(doctorId))
                .build();
    }
}
