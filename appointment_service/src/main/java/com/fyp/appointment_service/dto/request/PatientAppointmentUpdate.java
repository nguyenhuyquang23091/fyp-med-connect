package com.fyp.appointment_service.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PatientAppointmentUpdate {
    String userId;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    String reasons;

    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    String note;

    String doctorId;

    @Future(message = "Appointment date must be in the future")
    LocalDate appointment_date;
}