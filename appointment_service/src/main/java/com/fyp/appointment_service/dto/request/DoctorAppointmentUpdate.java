package com.fyp.appointment_service.dto.request;

import jakarta.validation.constraints.Future;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorAppointmentUpdate {
    String userId;
    String doctorId;

    @Future(message = "Appointment date must be in the future")
    LocalDate appointment_date;
}