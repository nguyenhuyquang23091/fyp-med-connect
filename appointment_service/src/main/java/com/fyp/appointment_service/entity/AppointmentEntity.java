package com.fyp.appointment_service.entity;

import com.fyp.appointment_service.constant.AppointmentStatus;
import com.fyp.appointment_service.constant.ConsultationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level =  AccessLevel.PRIVATE)
@Table(name = "appointments")
public class AppointmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String userId;

    String patientFullName;
    String doctorFullName;
    String doctorId;
    String reasons;
    String phoneNumber;
    LocalDateTime appointmentDateTime;
    Instant createdDate;
    String specialty;
    String services;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    AppointmentStatus appointmentStatus;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ConsultationType consultationType;
    Instant modifiedDate;
    BigDecimal prices;
}
