package com.fyp.statistic_service.entity;


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
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "statistic_event_log", indexes = {
        @Index(name = "idx_event_log_event_id", columnList = "eventId", unique = true),
        @Index(name = "idx_event_log_processed_at", columnList = "processedAt"),
        @Index(name = "idx_event_log_status", columnList = "status")
})

public class StatisticEventLogs {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false, unique = true, length = 500)
    String eventId;

    @Column(nullable = false, length = 100)
    String eventType;

    @Column(length = 255)
    String topic;

    @Column(nullable = false)
    @Builder.Default
    LocalDateTime processedAt = LocalDateTime.now();

    @Column(nullable = false, length = 50)
    @Builder.Default
    String status = "SUCCESS";

    @Column(columnDefinition = "TEXT")
    String errorMessage;

    @Column(nullable = false)
    @Builder.Default
    Integer retryCount = 0;

    @Column(columnDefinition = "TEXT")
    String additionalContext;

}
