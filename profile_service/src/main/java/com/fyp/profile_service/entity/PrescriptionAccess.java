package com.fyp.profile_service.entity;

import java.time.Instant;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.fyp.profile_service.constant.AccessStatus;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "prescription_access")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrescriptionAccess {
    @MongoId
    String id;

    String prescriptionId;
    String prescriptionName;
    String patientUserId;
    String doctorUserId;

    @Enumerated(EnumType.STRING)
    AccessStatus accessStatus;

    Instant requestedAt;
    Instant respondedAt;
    String requestReason;

    @Indexed(expireAfter = "60s")
    Instant expiresAt;
}
