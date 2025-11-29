package com.fyp.profile_service.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.fyp.profile_service.dto.request.ApiResponse;
import com.fyp.profile_service.dto.request.SpecialtyRequest;
import com.fyp.profile_service.dto.response.SpecialtyResponse;
import com.fyp.profile_service.service.SpecialtyService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/specialties")
@Validated
public class SpecialtyController {

    SpecialtyService specialtyService;

    @GetMapping("/getAll")
    public ApiResponse<List<SpecialtyResponse>> getAllSpecialties() {
        return ApiResponse.<List<SpecialtyResponse>>builder()
                .result(specialtyService.getAllSpecialties())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<SpecialtyResponse> getSpecialtyById(
            @PathVariable @NotBlank(message = "Specialty ID cannot be blank") String id) {
        return ApiResponse.<SpecialtyResponse>builder()
                .result(specialtyService.getSpecialtyById(id))
                .build();
    }

    @GetMapping("/code/{code}")
    public ApiResponse<SpecialtyResponse> getSpecialtyByCode(
            @PathVariable @NotBlank(message = "Specialty code cannot be blank") String code) {
        return ApiResponse.<SpecialtyResponse>builder()
                .result(specialtyService.getSpecialtyByCode(code))
                .build();
    }

    @PostMapping("/admin")
    public ApiResponse<SpecialtyResponse> createSpecialty(@Valid @RequestBody SpecialtyRequest request) {
        return ApiResponse.<SpecialtyResponse>builder()
                .result(specialtyService.createSpecialty(request))
                .build();
    }

    @PutMapping("/admin/{id}")
    public ApiResponse<SpecialtyResponse> updateSpecialty(
            @PathVariable @NotBlank(message = "Specialty ID cannot be blank") String id,
            @Valid @RequestBody SpecialtyRequest request) {
        return ApiResponse.<SpecialtyResponse>builder()
                .result(specialtyService.updateSpecialty(id, request))
                .build();
    }

    @DeleteMapping("/admin/{id}")
    public ApiResponse<Void> deleteSpecialty(
            @PathVariable @NotBlank(message = "Specialty ID cannot be blank") String id) {
        specialtyService.deleteSpecialty(id);
        return ApiResponse.<Void>builder().build();
    }
}
