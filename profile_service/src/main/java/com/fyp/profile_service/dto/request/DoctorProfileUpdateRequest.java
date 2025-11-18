package com.fyp.profile_service.dto.request;

import java.util.List;

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
