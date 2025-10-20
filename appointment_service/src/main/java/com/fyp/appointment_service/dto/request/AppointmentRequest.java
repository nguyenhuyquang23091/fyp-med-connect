package com.fyp.appointment_service.dto.request;

import com.fyp.appointment_service.constant.ConsultationType;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentRequest {

    @NotBlank(message = "Doctor ID is required")
    String doctorId;

    @NotBlank(message = "Appointment reason is required")
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    String reasons;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    String phoneNumber;

    @NotNull(message = "Appointment date and time is required")
    @Future(message = "Appointment date and time must be in the future")
    LocalDateTime appointmentDateTime;

    @NotNull(message = "Specialty relationship ID is required")
    Long specialtyRelationshipId;

    @NotNull(message = "Service relationship ID is required")
    Long serviceRelationshipId;

    @NotNull(message = "Consultation type is required")
    ConsultationType consultationType;
}