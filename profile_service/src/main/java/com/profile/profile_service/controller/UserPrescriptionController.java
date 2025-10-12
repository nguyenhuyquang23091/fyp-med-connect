package com.profile.profile_service.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.profile.profile_service.dto.request.ApiResponse;
import com.profile.profile_service.dto.request.DoctorPrescriptionUpdateRequest;
import com.profile.profile_service.dto.request.PatientPrescriptionCreateRequest;
import com.profile.profile_service.dto.request.PatientPrescriptionUpdateRequest;
import com.profile.profile_service.dto.request.PrescriptionNotification;
import com.profile.profile_service.dto.response.PrescriptionAccessResponse;
import com.profile.profile_service.dto.response.PrescriptionGeneralResponse;
import com.profile.profile_service.dto.response.PrescriptionResponse;
import com.profile.profile_service.service.PrescriptionAccessService;
import com.profile.profile_service.service.UserPrescriptionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/prescription")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class UserPrescriptionController {

    UserPrescriptionService userPrescriptionService;
    PrescriptionAccessService prescriptionAccessService;

    // Patient prescription endpoints
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PrescriptionResponse> createPrescription(
            @Valid @RequestPart(value = "request") PatientPrescriptionCreateRequest request,
            @RequestPart(value = "prescriptionImages", required = false) List<MultipartFile> prescriptionImages) {
        return ApiResponse.<PrescriptionResponse>builder()
                .result(userPrescriptionService.createPrescription(request, prescriptionImages))
                .build();
    }

    @GetMapping("/myPrescription")
    public ApiResponse<List<PrescriptionGeneralResponse>> getMyGeneralPrescriptions() {
        return ApiResponse.<List<PrescriptionGeneralResponse>>builder()
                .result(userPrescriptionService.getMyPrescriptions())
                .build();
    }

    @GetMapping("/myPrescription/{prescriptionId}")
    public ApiResponse<PrescriptionResponse> getMyDetailPrescription(@PathVariable String prescriptionId) {
        return ApiResponse.<PrescriptionResponse>builder()
                .result(userPrescriptionService.getMyPrescription(prescriptionId))
                .build();
    }

    @PutMapping(value = "/myPrescription/{prescriptionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PrescriptionResponse> updateMyPrescription(
            @PathVariable @NotBlank(message = "Prescription ID cannot be blank") String prescriptionId,
            @Valid @RequestPart(value = "updateRequest") PatientPrescriptionUpdateRequest request,
            @RequestPart(value = "updateImage", required = false) List<MultipartFile> updateImages) {
        return ApiResponse.<PrescriptionResponse>builder()
                .result(userPrescriptionService.updateMyPrescription(prescriptionId, request, updateImages))
                .build();
    }

    @DeleteMapping("/myPrescription/{prescriptionId}")
    public ApiResponse<Void> deleteMyPrescription(
            @PathVariable @NotBlank(message = "Prescription ID cannot be blank") String prescriptionId) {
        userPrescriptionService.deleteMyPrescription(prescriptionId);
        return ApiResponse.<Void>builder()
                .message("Prescription deleted successfully")
                .build();
    }

    @PutMapping("/access-request/{requestId}/approve")
    public ApiResponse<PrescriptionAccessResponse> approveAccessRequest(
            @PathVariable @NotBlank(message = "Request ID cannot be blank") String requestId) {
        return ApiResponse.<PrescriptionAccessResponse>builder()
                .result(prescriptionAccessService.approveRequest(requestId))
                .build();
    }

    @PutMapping("/access-request/{requestId}/deny")
    public ApiResponse<PrescriptionAccessResponse> denyAccessRequest(
            @PathVariable @NotBlank(message = "Request ID cannot be blank") String requestId) {
        return ApiResponse.<PrescriptionAccessResponse>builder()
                .result(prescriptionAccessService.denyRequest(requestId))
                .build();
    }

    // Doctor prescription endpoints
    @GetMapping("/doctor/{patientId}")
    public ApiResponse<List<PrescriptionGeneralResponse>> getGeneralPatientPrescription(
            @PathVariable @NotBlank(message = "Patient ID cannot be blank") String patientId) {
        return ApiResponse.<List<PrescriptionGeneralResponse>>builder()
                .result(userPrescriptionService.getGeneralPrescription(patientId))
                .build();
    }

    @PostMapping("/doctor/{patientId}/{prescriptionId}/request-access")
    public ApiResponse<PrescriptionAccessResponse> requestPrescriptionAccess(
            @PathVariable @NotBlank(message = "Patient ID cannot be blank") String patientId,
            @PathVariable @NotBlank(message = "Prescription ID cannot be blank") String prescriptionId,
            @RequestBody PrescriptionNotification.RequestReason requestReason) {
        return ApiResponse.<PrescriptionAccessResponse>builder()
                .result(userPrescriptionService.requestPrescriptionAccess(prescriptionId, patientId, requestReason))
                .build();
    }

    @PutMapping("/doctor/prescription/{prescriptionId}")
    public ApiResponse<PrescriptionResponse> doctorUpdatePrescription(
            @PathVariable @NotBlank(message = "Prescription ID cannot be blank") String prescriptionId,
            @Valid @RequestBody DoctorPrescriptionUpdateRequest request) {
        return ApiResponse.<PrescriptionResponse>builder()
                .result(userPrescriptionService.doctorUpdatePrescription(prescriptionId, request))
                .build();
    }

    @GetMapping("/doctor/{patientId}/{prescriptionId}")
    public ApiResponse<PrescriptionResponse> getPatientPrescription(
            @PathVariable @NotBlank(message = "Patient ID cannot be blank") String patientId,
            @PathVariable @NotBlank(message = "Prescription ID cannot be blank") String prescriptionId) {
        return ApiResponse.<PrescriptionResponse>builder()
                .result(userPrescriptionService.getOnePatientPrescription(patientId, prescriptionId))
                .build();
    }

    @DeleteMapping("/doctor/prescription/{prescriptionId}")
    public ApiResponse<Void> doctorDeletePrescription(
            @PathVariable @NotBlank(message = "Prescription ID cannot be blank") String prescriptionId) {
        userPrescriptionService.doctorDeletePrescription(prescriptionId);
        return ApiResponse.<Void>builder()
                .message("User Prescription Deleted Success Fully")
                .build();
    }
}
