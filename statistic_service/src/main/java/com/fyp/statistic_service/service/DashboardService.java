package com.fyp.statistic_service.service;

import com.fyp.statistic_service.dto.response.DashboardStatisticsResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardService {

    UserStatisticService userStatisticService;
    AppointmentStatisticService appointmentStatisticService;
    PaymentStatisticService paymentStatisticService;


    public DashboardStatisticsResponse getDashboardSummaryInternal() {
        log.info("Generating dashboard summary for AI Tool");

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(7);
        LocalDate monthStart = today.minusDays(30);

        // Build user metrics
        DashboardStatisticsResponse.UserMetrics userMetrics = DashboardStatisticsResponse.UserMetrics.builder()
            .totalUsers(userStatisticService.getLatestTotalUsers())
            .weeklyActiveUsers(Math.round(userStatisticService.getWeeklyActiveUsers()))
            .monthlyActiveUsers(Math.round(userStatisticService.getMonthlyActiveUsers()))
            .newUsersToday(userStatisticService.getNewUsersByDateRange(today, today))
            .newUsersThisWeek(userStatisticService.getNewUsersByDateRange(weekStart, today))
            .newUsersThisMonth(userStatisticService.getNewUsersByDateRange(monthStart, today))
            .build();

        // Build appointment metrics
        DashboardStatisticsResponse.AppointmentMetrics appointmentMetrics = 
            DashboardStatisticsResponse.AppointmentMetrics.builder()
                .totalAppointmentsToday(appointmentStatisticService.getTotalAppointmentsByDateRange(today, today))
                .totalAppointmentsThisWeek(appointmentStatisticService.getTotalAppointmentsByDateRange(weekStart, today))
                .totalAppointmentsThisMonth(appointmentStatisticService.getTotalAppointmentsByDateRange(monthStart, today))
                .completedThisWeek(appointmentStatisticService.getCompletedAppointmentsByDateRange(weekStart, today))
                .cancelledThisWeek(appointmentStatisticService.getCancelledAppointmentsByDateRange(weekStart, today))
                .pendingThisWeek(appointmentStatisticService.getPendingAppointmentsByDateRange(weekStart, today))
                .avgCancellationRate(appointmentStatisticService.getAvgCancellationRate(weekStart, today))
                .build();

        // Build payment metrics
        DashboardStatisticsResponse.PaymentMetrics paymentMetrics = 
            DashboardStatisticsResponse.PaymentMetrics.builder()
                .revenueToday(paymentStatisticService.getTotalRevenueByDateRange(today, today))
                .revenueThisWeek(paymentStatisticService.getTotalRevenueByDateRange(weekStart, today))
                .revenueThisMonth(paymentStatisticService.getTotalRevenueByDateRange(monthStart, today))
                .transactionsToday(paymentStatisticService.getTransactionCountByDateRange(today, today))
                .transactionsThisWeek(paymentStatisticService.getTransactionCountByDateRange(weekStart, today))
                .avgTransactionAmount(paymentStatisticService.getAvgTransactionAmount(weekStart, today))
                .build();

        // Assemble complete response
        DashboardStatisticsResponse response = DashboardStatisticsResponse.builder()
            .generatedAt(LocalDateTime.now())
            .userMetrics(userMetrics)
            .appointmentMetrics(appointmentMetrics)
            .paymentMetrics(paymentMetrics)
            .build();

        log.info("Dashboard summary generated: {} users, {} appointments this month, ${} revenue this month",
                response.getUserMetrics().getTotalUsers(),
                response.getAppointmentMetrics().getTotalAppointmentsThisMonth(),
                response.getPaymentMetrics().getRevenueThisMonth());

        return response;
    }
}

