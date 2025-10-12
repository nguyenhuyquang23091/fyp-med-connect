package com.fyp.appointment_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentRequest {
    String userId;

    @NotBlank(message = "Appointment reason is required")
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    String reasons;

    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    String note;

    @NotBlank(message = "Doctor ID is required")
    String doctorId;

    @NotNull(message = "Appointment date is required")
    @FutureOrPresent(message = "Appointment date must be in the future or today")
    LocalDate appointment_date;
}