package com.fyp.profile_service.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for creating a user profile.
 *
 * Note: Role is NOT included here as it's managed by AuthService.
 * ProfileService only stores profile-specific information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileCreationRequest {
    @NotBlank(message = "User ID is required")
    String userId;

    String email;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    String lastName;

    @Size(max = 100, message = "City must not exceed 100 characters")
    String city;

    @NotNull(message = "Date of birth is required")
    LocalDate dob;

    String gender;
}
