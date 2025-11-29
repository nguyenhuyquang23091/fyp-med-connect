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
@Table(name = "payment_statistics", indexes = {
        @Index(name = "idx_payment_stats_date", columnList = "date")
})
public class PaymentStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false, unique = true)
    LocalDate date;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    BigDecimal averageTransactionAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    Long transactionCount = 0L;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    BigDecimal refundedAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    Long refundCount = 0L;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    Map<String, Long> paymentMethodBreakdown = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    Map<String, Long> paymentStatusBreakdown = new HashMap<>();

    @Column(nullable = false, updatable = false)
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (transactionCount > 0) {
            this.averageTransactionAmount = totalRevenue
                    .divide(BigDecimal.valueOf(transactionCount), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
}
