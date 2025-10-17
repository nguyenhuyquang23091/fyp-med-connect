package com.fyp.profile_service.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorProfileResponse {

    String firstName;
    String avatar;
    String lastName;
    String city;
    LocalDate dob;
    String gender;
    String email;

    String id;
    String userId;
    String residency;
    Integer yearsOfExperience;
    String bio;
    Boolean isAvailable;
    List<String> languages;

    // Relationship data
    List<DoctorSpecialtyResponse> specialties;
    List<DoctorServiceResponse> services;
    List<DoctorExperienceResponse> practiceExperiences;

    // Audit fields
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
