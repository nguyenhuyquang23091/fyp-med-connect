package com.fyp.statistic_service.controller;

import com.fyp.statistic_service.dto.request.ApiResponse;
import com.fyp.statistic_service.dto.response.DashboardStatisticsResponse;
import com.fyp.statistic_service.service.AppointmentStatisticService;
import com.fyp.statistic_service.service.DashboardService;
import com.fyp.statistic_service.service.PaymentStatisticService;
import com.fyp.statistic_service.service.UserStatisticService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DashboardController {

    DashboardService dashboardService;
    UserStatisticService userStatisticService;
    AppointmentStatisticService appointmentStatisticService;
    PaymentStatisticService paymentStatisticService;


    /**
     * Get comprehensive dashboard summary with metrics across all domains.
     * Uses DashboardService to aggregate statistics from all services.
     * 
     * @return Dashboard statistics including user, appointment, and payment metrics
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/summary")
    ApiResponse<DashboardStatisticsResponse> getDashboardSummary() {
        log.info("Fetching dashboard summary");

        DashboardStatisticsResponse summary = dashboardService.getDashboardSummaryInternal();

        return ApiResponse.<DashboardStatisticsResponse>builder()
                .result(summary)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
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


    @PreAuthorize("hasRole('ADMIN')")
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


    @PreAuthorize("hasRole('ADMIN')")
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


    @PreAuthorize("hasRole('ADMIN')")
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
