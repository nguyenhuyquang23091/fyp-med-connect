package com.fyp.notification_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class VideoCallNotificationRequest extends NotificationRequest {
    String roomId;
    String appointmentId;
    String doctorId;
    String patientId;
}
