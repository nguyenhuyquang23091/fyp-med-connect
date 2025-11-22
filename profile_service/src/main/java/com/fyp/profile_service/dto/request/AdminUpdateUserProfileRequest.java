package com.fyp.profile_service.dto.request;

import java.time.LocalDate;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUpdateUserProfileRequest {
    String firstName;
    String lastName;
    String city;
    LocalDate dob;
    String gender;
    String email;
}
