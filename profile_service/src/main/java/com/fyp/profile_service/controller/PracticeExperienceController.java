package com.fyp.profile_service.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.fyp.profile_service.dto.request.ApiResponse;
import com.fyp.profile_service.dto.request.PracticeExperienceRequest;
import com.fyp.profile_service.dto.response.PracticeExperienceResponse;
import com.fyp.profile_service.service.PracticeExperienceService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/practice-experiences")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class PracticeExperienceController {

    PracticeExperienceService practiceExperienceService;

    @GetMapping
    public ApiResponse<List<PracticeExperienceResponse>> getMyPracticeExperiences() {
        return ApiResponse.<List<PracticeExperienceResponse>>builder()
                .result(practiceExperienceService.getMyPracticeExperiences())
                .build();
    }

    @PostMapping
    public ApiResponse<PracticeExperienceResponse> addPracticeExperience(
            @Valid @RequestBody PracticeExperienceRequest request) {
        return ApiResponse.<PracticeExperienceResponse>builder()
                .result(practiceExperienceService.addPracticeExperience(request))
                .build();
    }

    @PutMapping("/{experienceId}")
    public ApiResponse<PracticeExperienceResponse> updatePracticeExperience(
            @PathVariable @NotBlank(message = "Experience ID cannot be blank") String experienceId,
            @Valid @RequestBody PracticeExperienceRequest request) {
        return ApiResponse.<PracticeExperienceResponse>builder()
                .result(practiceExperienceService.updatePracticeExperience(experienceId, request))
                .build();
    }

    @DeleteMapping("/{experienceId}")
    public ApiResponse<Void> deletePracticeExperience(
            @PathVariable @NotBlank(message = "Experience ID cannot be blank") String experienceId) {
        practiceExperienceService.deletePracticeExperience(experienceId);
        return ApiResponse.<Void>builder().build();
    }

    @PatchMapping("/{experienceId}/highlight")
    public ApiResponse<PracticeExperienceResponse> highlightPracticeExperience(
            @PathVariable @NotBlank(message = "Experience ID cannot be blank") String experienceId,
            @RequestParam boolean isHighlighted) {
        return ApiResponse.<PracticeExperienceResponse>builder()
                .result(practiceExperienceService.highlightPracticeExperience(experienceId, isHighlighted))
                .build();
    }
}
