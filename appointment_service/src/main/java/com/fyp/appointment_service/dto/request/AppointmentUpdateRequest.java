package com.fyp.appointment_service.dto.request;


import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentUpdateRequest {


    @NotBlank(message = "Appointment reason is required")
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    String reasons;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    String phoneNumber;

    @NotNull(message = "Appointment date and time is required")
    @Future(message = "Appointment date and time must be in the future")
    LocalDateTime appointmentDateTime;


}
