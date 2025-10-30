package com.fyp.search_service.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorProfileResponse {
    String doctorProfileId;
    String userId;
    //thiếu doctorFullName + Email

    // Basic doctor information
    String residency;
    Integer yearsOfExperience;
    String bio;
    Boolean isAvailable;
    List<String> languages;

    // Nested objects
    List<ServiceResponse> services;
    List<SpecialtyResponse> specialties;
    List<ExperienceResponse> experiences;

    /**
     * Service information response DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ServiceResponse {
        Long relationshipId;
        Long serviceId;
        String serviceName;
        BigDecimal price;
        String currency;
        Integer displayOrder;
    }

    /**
     * Specialty information response DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SpecialtyResponse {
        Long relationshipId;
        Long specialtyId;
        String specialtyName;
        String specialtyCode;
        String specialtyDescription;
        Boolean isPrimary;
        LocalDate certificationDate;
        String certificationBody;
        Integer yearsOfExperienceInSpecialty;
    }

    /**
     * Experience information response DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ExperienceResponse {
        Long relationshipId;
        String experienceId;
        String hospitalName;
        String hospitalLogo;
        String department;
        String location;
        String country;
        String position;
        LocalDate startDate;
        LocalDate endDate;
        Boolean isCurrent;
        String description;
        Integer displayOrder;
        Boolean isHighlighted;
    }
}
