package com.fyp.profile_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicalServiceRequest {

    @NotBlank(message = "Medical service name is required")
    @Size(min = 2, max = 150, message = "Service name must be between 2 and 150 characters")
    String name;
}
