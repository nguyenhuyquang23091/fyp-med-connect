package com.fyp.statistic_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserStatisticResponse {
    String id;
    LocalDate date;
    Long totalUsers;
    Long newUsers;
    Long activeUsers;
    Long doctorCount;
    Long patientCount;
    Long adminCount;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

