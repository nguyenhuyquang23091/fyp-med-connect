package com.fyp.appointment_service.dto.request;

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
    String reasons;
    String note;
    String doctorId;
    LocalDate appointment_date;
}