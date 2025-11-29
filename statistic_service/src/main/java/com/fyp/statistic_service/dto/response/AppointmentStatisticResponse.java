package com.fyp.statistic_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentStatisticResponse {
    String id;
    LocalDate date;
    Long totalAppointments;
    Long completedAppointments;
    Long cancelledAppointments;
    Long pendingAppointments;
    Long inProgressAppointments;
    BigDecimal cancellationRate;
    Map<String, Long> consultationTypeBreakdown;
    Map<String, Long> departmentBreakdown;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

