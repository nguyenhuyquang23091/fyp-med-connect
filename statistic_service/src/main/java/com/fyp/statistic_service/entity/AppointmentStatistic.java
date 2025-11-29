package com.fyp.statistic_service.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "appointment_statistics", indexes = {
        @Index(name = "idx_appointment_stats_date", columnList = "date")
})
public class AppointmentStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false, unique = true)
    LocalDate date;

    @Column(nullable = false)
    @Builder.Default
    Long totalAppointments = 0L;

    @Column(nullable = false)
    @Builder.Default
    Long completedAppointments = 0L;

    @Column(nullable = false)
    @Builder.Default
    Long cancelledAppointments = 0L;

    @Column(nullable = false)
    @Builder.Default
    Long pendingAppointments = 0L;

    @Column(nullable = false)
    @Builder.Default
    Long inProgressAppointments = 0L;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    BigDecimal cancellationRate = BigDecimal.ZERO;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    Map<String, Long> consultationTypeBreakdown = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    Map<String, Long> departmentBreakdown = new HashMap<>();

    @Column(nullable = false, updatable = false)
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (totalAppointments > 0) {
            this.cancellationRate = BigDecimal.valueOf(cancelledAppointments)
                    .divide(BigDecimal.valueOf(totalAppointments), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
    }
}
