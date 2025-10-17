package com.fyp.profile_service.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorSpecialtyRequest {

    @NotBlank(message = "Specialty ID is required")
    String specialtyId;

    @NotNull(message = "Primary specialty flag is required")
    Boolean isPrimary;

    @PastOrPresent(message = "Certification date cannot be in the future")
    LocalDate certificationDate;

    @Size(max = 200, message = "Certification body must not exceed 200 characters")
    String certificationBody;

    @Min(value = 0, message = "Years of experience must be non-negative")
    @Max(value = 70, message = "Years of experience must not exceed 70")
    Integer yearsOfExperienceInSpecialty;
}
