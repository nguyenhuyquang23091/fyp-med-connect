package com.fyp.appointment_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorExperienceResponse {

    Long relationshipId;
    PracticeExperienceResponse experience;
    Integer displayOrder;
    Boolean isHighlighted;
}
