package com.fyp.search_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentResponse {
    String id;
    String userId;
    String patientFullName;
    String doctorFullName;
    String doctorId;
    String reasons;
    String phoneNumber;
    String appointmentDateTime;
    String createdDate;
    String specialty;
    String services;
    String appointmentStatus;
    String consultationType;
    String paymentMethod;
    String modifiedDate;
    BigDecimal prices;
}
