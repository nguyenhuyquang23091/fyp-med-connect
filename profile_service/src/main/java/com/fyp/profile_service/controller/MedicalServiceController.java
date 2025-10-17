package com.fyp.profile_service.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.fyp.profile_service.dto.request.ApiResponse;
import com.fyp.profile_service.dto.request.MedicalServiceRequest;
import com.fyp.profile_service.dto.response.MedicalServiceResponse;
import com.fyp.profile_service.service.MedicalServiceService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/medical-services")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class MedicalServiceController {

    MedicalServiceService medicalServiceService;

    @GetMapping
    public ApiResponse<List<MedicalServiceResponse>> getAllMedicalServices() {
        return ApiResponse.<List<MedicalServiceResponse>>builder()
                .result(medicalServiceService.getAllMedicalServices())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<MedicalServiceResponse> getMedicalServiceById(
            @PathVariable @NotBlank(message = "Medical service ID cannot be blank") String id) {
        return ApiResponse.<MedicalServiceResponse>builder()
                .result(medicalServiceService.getMedicalServiceById(id))
                .build();
    }

    @PostMapping("/admin")
    public ApiResponse<MedicalServiceResponse> createMedicalService(@Valid @RequestBody MedicalServiceRequest request) {
        return ApiResponse.<MedicalServiceResponse>builder()
                .result(medicalServiceService.createMedicalService(request))
                .build();
    }

    @PutMapping("/admin/{id}")
    public ApiResponse<MedicalServiceResponse> updateMedicalService(
            @PathVariable @NotBlank(message = "Medical service ID cannot be blank") String id,
            @Valid @RequestBody MedicalServiceRequest request) {
        return ApiResponse.<MedicalServiceResponse>builder()
                .result(medicalServiceService.updateMedicalService(id, request))
                .build();
    }

    @DeleteMapping("/admin/{id}")
    public ApiResponse<Void> deleteMedicalService(
            @PathVariable @NotBlank(message = "Medical service ID cannot be blank") String id) {
        medicalServiceService.deleteMedicalService(id);
        return ApiResponse.<Void>builder().build();
    }
}
