package com.fyp.notification_service.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {

    String id;
    String recipientUserId;

    String notificationType;

    String title;

    String message;

    Boolean isRead;

    Boolean isProcessed;

    Instant createdAt;

    Instant readAt;

    Map<String, Object> metadata;
}
