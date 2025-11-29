package com.fyp.statistic_service.controller;

import com.fyp.statistic_service.dto.request.ApiResponse;
import com.fyp.statistic_service.service.AppointmentStatisticService;
import com.fyp.statistic_service.service.PaymentStatisticService;
import com.fyp.statistic_service.service.UserStatisticService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DashboardController {

    UserStatisticService userStatisticService;
    AppointmentStatisticService appointmentStatisticService;
    PaymentStatisticService paymentStatisticService;


    @GetMapping("/summary")
    ApiResponse<Map<String, Object>> getDashboardSummary() {
        log.info("Fetching dashboard summary");

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(7);
        LocalDate monthStart = today.minusDays(30);

        Map<String, Object> summary = new HashMap<>();

        Map<String, Object> userMetrics = new HashMap<>();
        userMetrics.put("totalUsers", userStatisticService.getLatestTotalUsers());
        userMetrics.put("weeklyActiveUsers", userStatisticService.getWeeklyActiveUsers());
        userMetrics.put("monthlyActiveUsers", userStatisticService.getMonthlyActiveUsers());
        userMetrics.put("newUsersToday", userStatisticService.getNewUsersByDateRange(today, today));
        userMetrics.put("newUsersThisWeek", userStatisticService.getNewUsersByDateRange(weekStart, today));
        userMetrics.put("newUsersThisMonth", userStatisticService.getNewUsersByDateRange(monthStart, today));
        summary.put("userMetrics", userMetrics);

        Map<String, Object> appointmentMetrics = new HashMap<>();
        appointmentMetrics.put("totalAppointmentsToday", appointmentStatisticService.getTotalAppointmentsByDateRange(today, today));
        appointmentMetrics.put("totalAppointmentsThisWeek", appointmentStatisticService.getTotalAppointmentsByDateRange(weekStart, today));
        appointmentMetrics.put("totalAppointmentsThisMonth", appointmentStatisticService.getTotalAppointmentsByDateRange(monthStart, today));
        appointmentMetrics.put("completedThisWeek", appointmentStatisticService.getCompletedAppointmentsByDateRange(weekStart, today));
        appointmentMetrics.put("cancelledThisWeek", appointmentStatisticService.getCancelledAppointmentsByDateRange(weekStart, today));
        appointmentMetrics.put("pendingThisWeek", appointmentStatisticService.getPendingAppointmentsByDateRange(weekStart, today));
        appointmentMetrics.put("avgCancellationRate", appointmentStatisticService.getAvgCancellationRate(weekStart, today));
        summary.put("appointmentMetrics", appointmentMetrics);

        Map<String, Object> paymentMetrics = new HashMap<>();
        paymentMetrics.put("revenueToday", paymentStatisticService.getTotalRevenueByDateRange(today, today));
        paymentMetrics.put("revenueThisWeek", paymentStatisticService.getTotalRevenueByDateRange(weekStart, today));
        paymentMetrics.put("revenueThisMonth", paymentStatisticService.getTotalRevenueByDateRange(monthStart, today));
        paymentMetrics.put("transactionsToday", paymentStatisticService.getTransactionCountByDateRange(today, today));
        paymentMetrics.put("transactionsThisWeek", paymentStatisticService.getTransactionCountByDateRange(weekStart, today));
        paymentMetrics.put("avgTransactionAmount", paymentStatisticService.getAvgTransactionAmount(weekStart, today));
        summary.put("paymentMetrics", paymentMetrics);

        return ApiResponse.<Map<String, Object>>builder()
                .result(summary)
                .build();
    }

    @GetMapping("/today")
    ApiResponse<Map<String, Object>> getTodayMetrics() {
        log.info("Fetching today's metrics");

        LocalDate today = LocalDate.now();
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("newUsers", userStatisticService.getNewUsersByDateRange(today, today));
        metrics.put("totalAppointments", appointmentStatisticService.getTotalAppointmentsByDateRange(today, today));
        metrics.put("revenue", paymentStatisticService.getTotalRevenueByDateRange(today, today));
        metrics.put("transactions", paymentStatisticService.getTransactionCountByDateRange(today, today));

        return ApiResponse.<Map<String, Object>>builder()
                .result(metrics)
                .build();
    }


    @GetMapping("/week")
    ApiResponse<Map<String, Object>> getWeekMetrics() {
        log.info("Fetching weekly metrics");

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(7);
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("newUsers", userStatisticService.getNewUsersByDateRange(weekStart, today));
        metrics.put("activeUsers", userStatisticService.getWeeklyActiveUsers());
        metrics.put("totalAppointments", appointmentStatisticService.getTotalAppointmentsByDateRange(weekStart, today));
        metrics.put("completedAppointments", appointmentStatisticService.getCompletedAppointmentsByDateRange(weekStart, today));
        metrics.put("cancelledAppointments", appointmentStatisticService.getCancelledAppointmentsByDateRange(weekStart, today));
        metrics.put("cancellationRate", appointmentStatisticService.getAvgCancellationRate(weekStart, today));
        metrics.put("revenue", paymentStatisticService.getTotalRevenueByDateRange(weekStart, today));
        metrics.put("transactions", paymentStatisticService.getTransactionCountByDateRange(weekStart, today));

        return ApiResponse.<Map<String, Object>>builder()
                .result(metrics)
                .build();
    }


    @GetMapping("/month")
    ApiResponse<Map<String, Object>> getMonthMetrics() {
        log.info("Fetching monthly metrics");

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.minusDays(30);
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("newUsers", userStatisticService.getNewUsersByDateRange(monthStart, today));
        metrics.put("activeUsers", userStatisticService.getMonthlyActiveUsers());
        metrics.put("totalAppointments", appointmentStatisticService.getTotalAppointmentsByDateRange(monthStart, today));
        metrics.put("completedAppointments", appointmentStatisticService.getCompletedAppointmentsByDateRange(monthStart, today));
        metrics.put("cancelledAppointments", appointmentStatisticService.getCancelledAppointmentsByDateRange(monthStart, today));
        metrics.put("revenue", paymentStatisticService.getTotalRevenueByDateRange(monthStart, today));
        metrics.put("transactions", paymentStatisticService.getTransactionCountByDateRange(monthStart, today));

        return ApiResponse.<Map<String, Object>>builder()
                .result(metrics)
                .build();
    }


    @GetMapping("/trends")
    ApiResponse<Map<String, Object>> getTrends() {
        log.info("Fetching trends data");

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        Map<String, Object> trends = new HashMap<>();
        trends.put("userGrowthTrend", userStatisticService.getUserGrowthTrend(startDate, endDate));
        trends.put("cancellationRateTrend", appointmentStatisticService.getCancellationRateTrend(startDate, endDate));
        trends.put("revenueTrend", paymentStatisticService.getRevenueTrend(startDate, endDate));

        return ApiResponse.<Map<String, Object>>builder()
                .result(trends)
                .build();
    }
}
