package com.fyp.appointment_service.dto.request;

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
    LocalDate appointment_date;
}