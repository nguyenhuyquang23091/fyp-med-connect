package com.fyp.profile_service.dto.request;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fyp.profile_service.entity.DoctorExperienceRelationship;
import com.fyp.profile_service.entity.DoctorServiceRelationship;
import com.fyp.profile_service.entity.DoctorSpecialtyRelationship;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorProfileUpdateRequest {

    String residency;

    Integer yearsOfExperience;

    String bio;

    Boolean isAvailable;

    List<String> languages;
}
