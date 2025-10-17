package com.fyp.profile_service.dto.response;

import java.time.LocalDate;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
