package com.fyp.profile_service.dto.response;

import java.time.Instant;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrescriptionGeneralResponse {
    String id;
    String userId;
    String userProfileId;
    String prescriptionName;
    // MedicalInformation
    String doctorId;
    // Status
    String status;
    String accessStatus;
    Instant createdAt;
    Instant updatedAt;
}
