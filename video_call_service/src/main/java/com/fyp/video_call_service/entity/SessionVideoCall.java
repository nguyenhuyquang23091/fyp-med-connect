package com.fyp.video_call_service.entity;


import com.fyp.video_call_service.constant.SessionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level =  AccessLevel.PRIVATE)
@Table(name = "video_call_sessions",
        uniqueConstraints = @UniqueConstraint(columnNames = "roomId")

)
public class SessionVideoCall {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String appointmentId;

    @Column(nullable = false, unique = true)
    String roomId;
    String doctorId;
    String patientId;
    @Enumerated(EnumType.STRING)
    SessionStatus sessionStatus;

    LocalDateTime scheduledStartTime;
    LocalDateTime actualStartTime;
    LocalDateTime actualEndTime;
    LocalDateTime expiryTime;   // Auto-close if not joined
    Integer duration;           // In minutes
    LocalDateTime createdAt;



}
