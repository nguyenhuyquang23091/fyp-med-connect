package com.fyp.appointment_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorSpecialtyResponse {

    Long relationshipId;
    SpecialtyResponse specialty;
    Boolean isPrimary;
    LocalDate certificationDate;
    String certificationBody;
    Integer yearsOfExperienceInSpecialty;
}
