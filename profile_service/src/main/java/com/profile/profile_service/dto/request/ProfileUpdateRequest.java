package com.profile.profile_service.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileUpdateRequest {
    @Size(max = 50, message = "First name must not exceed 50 characters")
    String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    String lastName;

    @Size(max = 100, message = "City must not exceed 100 characters")
    String city;

    LocalDate dob;
}
