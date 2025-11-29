package com.fyp.statistic_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatisticEventLogsResponse {
    String id;
    String eventId;
    String eventType;
    String topic;
    LocalDateTime processedAt;
    String status;
    String errorMessage;
    Integer retryCount;
    String additionalContext;
}

