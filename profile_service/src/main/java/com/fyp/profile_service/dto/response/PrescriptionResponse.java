package com.fyp.profile_service.dto.response;

import java.time.Instant;
import java.util.List;

import com.fyp.profile_service.entity.UserPrescription;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrescriptionResponse {
    String id;
    String userId;
    String userProfileId;
    String prescriptionName;
    List<String> imageURLS;
    List<UserPrescription.PrescriptionData> prescriptionData;
    // MedicalInformation
    String doctorId;
    // Status
    String status;
    String accessStatus;
    Instant createdAt;
    Instant updatedAt;
}
