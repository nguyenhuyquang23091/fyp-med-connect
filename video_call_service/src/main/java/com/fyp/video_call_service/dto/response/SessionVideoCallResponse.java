package com.fyp.video_call_service.dto.response;

import com.fyp.video_call_service.constant.SessionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SessionVideoCallResponse {

    String id;
    String appointmentId;

    String roomId;
    String doctorId;
    String patientId;
    SessionStatus sessionStatus;

    LocalDateTime scheduledStartTime;
    LocalDateTime actualStartTime;
    LocalDateTime actualEndTime;
    LocalDateTime expiryTime;   // Auto-close if not joined
    Integer duration;           // In minutes
    LocalDateTime createdAt;


}
