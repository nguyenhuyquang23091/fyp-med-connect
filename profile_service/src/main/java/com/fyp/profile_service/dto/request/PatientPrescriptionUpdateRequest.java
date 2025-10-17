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
public class PatientPrescriptionUpdateRequest {

    @Size(max = 200, message = "Prescription name must not exceed 200 characters")
    String prescriptionName;

    List<String> imageURLS;

    @Valid
    List<PatientPrescriptionDataUpdate> prescriptionData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientPrescriptionDataUpdate {

        @Pattern(regexp = "^\\d+(\\.\\d+)?$", message = "Blood sugar level must be a valid number")
        String bloodSugarLevel;

        @Pattern(regexp = "^(FASTING|BEFORE_MEAL|AFTER_MEAL|BEDTIME|RANDOM)$", message = "Invalid reading type")
        String readingType;

        String measurementDate;

        String bloodSugarCategory;
    }
}
