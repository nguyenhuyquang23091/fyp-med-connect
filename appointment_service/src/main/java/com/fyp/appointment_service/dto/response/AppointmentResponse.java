package com.fyp.appointment_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentResponse {
    String userId;
    String reasons;
    String note;
    String doctorId;
    LocalDate appointment_date;
    Instant createdDate;
    Instant modifiedDate;
}
