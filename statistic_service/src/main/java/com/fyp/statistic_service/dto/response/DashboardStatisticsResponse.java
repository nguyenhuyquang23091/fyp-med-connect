package com.fyp.statistic_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardStatisticsResponse {


    LocalDateTime generatedAt;


    UserMetrics userMetrics;

    AppointmentMetrics appointmentMetrics;


    PaymentMetrics paymentMetrics;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserMetrics {
        Long totalUsers;
        Long weeklyActiveUsers;
        Long monthlyActiveUsers;
        Long newUsersToday;
        Long newUsersThisWeek;
        Long newUsersThisMonth;
    }


    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class AppointmentMetrics {
        Long totalAppointmentsToday;
        Long totalAppointmentsThisWeek;
        Long totalAppointmentsThisMonth;
        Long completedThisWeek;
        Long cancelledThisWeek;
        Long pendingThisWeek;
        BigDecimal avgCancellationRate;
    }


    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PaymentMetrics {
        BigDecimal revenueToday;
        BigDecimal revenueThisWeek;
        BigDecimal revenueThisMonth;
        Long transactionsToday;
        Long transactionsThisWeek;
        BigDecimal avgTransactionAmount;
    }
}

