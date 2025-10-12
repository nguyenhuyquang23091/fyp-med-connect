package com.fyp.notification_service.entity;

import java.time.Instant;
import java.util.Map;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {
    @MongoId
    String id;

    @Indexed
    String recipientUserId;

    String notificationType;

    String title;

    String message;

    @Builder.Default
    Boolean isRead = false;

    Instant createdAt;

    Instant readAt;

    @Builder.Default
    Boolean isProcessed = false;

    Map<String, Object> metadata;

}
