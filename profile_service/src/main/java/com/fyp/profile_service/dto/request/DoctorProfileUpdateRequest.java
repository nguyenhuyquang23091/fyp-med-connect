package com.fyp.profile_service.dto.request;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fyp.profile_service.entity.DoctorExperienceRelationship;
import com.fyp.profile_service.entity.DoctorServiceRelationship;
import com.fyp.profile_service.entity.DoctorSpecialtyRelationship;

public class DoctorProfileUpdateRequest {

    String residency;

    Integer yearsOfExperience;

    String bio;

    Boolean isAvailable;

    List<String> languages;

    Set<DoctorSpecialtyRelationship> specialties = new HashSet<>();

    Set<DoctorExperienceRelationship> practiceExperience = new HashSet<>();

    Set<DoctorServiceRelationship> services = new HashSet<>();
}
