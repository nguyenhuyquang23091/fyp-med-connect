package com.fyp.profile_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpecialtyRequest {

    @NotBlank(message = "Specialty name is required")
    @Size(min = 2, max = 100, message = "Specialty name must be between 2 and 100 characters")
    String name;

    @NotBlank(message = "Specialty code is required")
    @Pattern(
            regexp = "^[A-Z0-9_]{2,20}$",
            message = "Specialty code must be uppercase letters, numbers, or underscores (2-20 characters)")
    String code;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description;
}
