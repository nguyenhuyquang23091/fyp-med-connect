package com.profile.profile_service.entity;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.profile.profile_service.constant.PrescriptionStatus;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_prescription")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserPrescription {
    @MongoId
    String id;

    String userId;
    String userProfileId;
    String prescriptionName;
    List<String> imageURLS;
    List<PrescriptionData> prescriptionData;
    // MedicalInformation
    String doctorId;
    // Status
    @Enumerated(EnumType.STRING)
    PrescriptionStatus status;

    Instant createdAt;
    Instant updatedAt;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrescriptionData {
        String medicationName;
        String dosage;
        String frequency;
        String instructions;
        String doctorNotes;
        String bloodSugarLevel;
        String readingType;
        String bloodSugarCategory;
        String measurementDate;
    }
}
