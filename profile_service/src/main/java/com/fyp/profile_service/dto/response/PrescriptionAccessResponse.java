package com.fyp.profile_service.dto.response;

import java.time.Instant;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrescriptionAccessResponse {
    String prescriptionId;
    String patientUserId;
    String doctorUserId;
    String accessStatus;
    String requestReason;
    String prescriptionName;
    Instant requestedAt;
    Instant respondedAt;
    Instant expiresAt;
}
