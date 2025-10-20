package com.fyp.appointment_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PracticeExperienceResponse {
    String id;
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
}
