package com.profile.profile_service.dto.response;

import java.time.LocalDate;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Response DTO for user profile information.
 *
 * Note: Role is NOT included here. If role information is needed,
 * it should be obtained from JWT token claims in the SecurityContext.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileResponse {
    String id;
    String userId;
    String firstName;
    String avatar;
    String lastName;
    String city;
    LocalDate dob;
}
