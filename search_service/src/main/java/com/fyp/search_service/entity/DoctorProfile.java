package com.fyp.search_service.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(indexName = "doctor_profiles")
public class DoctorProfile {
    @Id
    String doctorProfileId;
    String userId;

    // Basic doctor information
    String residency;
    Integer yearsOfExperience;
    String bio;
    Boolean isAvailable;
    List<String> languages;


    // Nested objects for relationships
    @Field(type = FieldType.Nested)
    @Builder.Default
    List<ServiceInfo> services = new ArrayList<>();

    @Field(type = FieldType.Nested)
    @Builder.Default
    List<SpecialtyInfo> specialties = new ArrayList<>();

    @Field(type = FieldType.Nested)
    @Builder.Default
    List<ExperienceInfo> experiences = new ArrayList<>();


    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ServiceInfo {
        Long relationshipId;
        Long serviceId;
        String serviceName;
        BigDecimal price;
        String currency;
        Integer displayOrder;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SpecialtyInfo {
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

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ExperienceInfo {
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
