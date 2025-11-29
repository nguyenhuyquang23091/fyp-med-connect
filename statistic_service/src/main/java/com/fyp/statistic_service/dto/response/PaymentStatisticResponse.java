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
public class PaymentStatisticResponse {
    String id;
    LocalDate date;
    BigDecimal totalRevenue;
    BigDecimal averageTransactionAmount;
    Long transactionCount;
    BigDecimal refundedAmount;
    Long refundCount;
    Map<String, Long> paymentMethodBreakdown;
    Map<String, Long> paymentStatusBreakdown;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

