package com.fyp.profile_service.dto.request;

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
public class PatientPrescriptionCreateRequest {

    @NotBlank(message = "Prescription name is required")
    @Size(max = 200, message = "Prescription name must not exceed 200 characters")
    String prescriptionName;

    @NotNull(message = "Prescription data is required")
    @NotEmpty(message = "At least one prescription entry is required")
    @Valid
    List<PatientPrescriptionDataRequest> prescriptionData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientPrescriptionDataRequest {

        @NotBlank(message = "Blood sugar level is required")
        @Pattern(regexp = "^\\d+(\\.\\d+)?$", message = "Blood sugar level must be a valid number")
        String bloodSugarLevel;

        @NotBlank(message = "Reading type is required")
        @Pattern(regexp = "^(FASTING|BEFORE_MEAL|AFTER_MEAL|BEDTIME|RANDOM)$", message = "Invalid reading type")
        String readingType;

        @NotBlank(message = "Measurement date is required")
        String measurementDate; // ISO-8601 format

        String bloodSugarCategory; // LOW, NORMAL, HIGH, VERY_HIGH
    }
}
