package com.fyp.appointment_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level =  AccessLevel.PRIVATE)
@Table(name = "users")
public class AppointmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String userId;
    String reasons;
    String note;
    String doctorId;
    LocalDate appointment_date;
    Instant createdDate;
    Instant modifiedDate;
}
