package com.fyp.profile_service.dto.request;

import jakarta.validation.constraints.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorExperienceUpdateRequest {

    @Min(value = 0, message = "Display order must be non-negative")
    @Max(value = 100, message = "Display order must not exceed 100")
    Integer displayOrder;

    @NotNull(message = "Highlight flag is required")
    Boolean isHighlighted;
}
