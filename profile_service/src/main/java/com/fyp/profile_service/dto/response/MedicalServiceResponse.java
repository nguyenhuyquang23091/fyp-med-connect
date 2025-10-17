package com.fyp.profile_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Response DTO for medical service.
 * Used for returning medical service information in API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicalServiceResponse {
    String id;
    String name;
}
