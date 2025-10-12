package com.profile.profile_service.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorPrescriptionUpdateRequest {
    @NotNull(message = "Prescription data is required")
    @NotEmpty(message = "At least one prescription entry is required")
    @Valid
    List<DoctorPrescriptionDataUpdate> prescriptionData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorPrescriptionDataUpdate {

        // Medical fields - doctor can update these
        @NotBlank(message = "Medication name is required")
        @Size(max = 200, message = "Medication name must not exceed 200 characters")
        String medicationName;

        @NotBlank(message = "Dosage is required")
        @Size(max = 100, message = "Dosage must not exceed 100 characters")
        String dosage;

        @NotBlank(message = "Frequency is required")
        @Size(max = 100, message = "Frequency must not exceed 100 characters")
        String frequency;

        @Size(max = 500, message = "Instructions must not exceed 500 characters")
        String instructions;

        @Size(max = 1000, message = "Doctor notes must not exceed 1000 characters")
        String doctorNotes;
    }
}
