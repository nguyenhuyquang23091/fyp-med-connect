package com.fyp.profile_service.dto.response;

import java.time.LocalDate;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
