package com.fyp.appointment_service.dto.response;

import com.fyp.appointment_service.constant.AppointmentStatus;
import com.fyp.appointment_service.constant.ConsultationType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentResponse {
    String id;
    String userId;
    String doctorId;

    String patientFullName;
    String doctorFullName;

    String reasons;
    String phoneNumber;

    LocalDateTime appointmentDateTime;

    String specialty;
    String services;

    AppointmentStatus appointmentStatus;
    ConsultationType consultationType;

    BigDecimal prices;

    String paymentURL;

    Instant createdDate;
    Instant modifiedDate;
}
