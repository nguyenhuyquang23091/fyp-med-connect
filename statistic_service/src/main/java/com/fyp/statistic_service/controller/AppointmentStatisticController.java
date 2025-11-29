package com.fyp.statistic_service.controller;

import com.fyp.statistic_service.dto.request.ApiResponse;
import com.fyp.statistic_service.dto.response.AppointmentStatisticResponse;
import com.fyp.statistic_service.service.AppointmentStatisticService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/appointment-statistics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AppointmentStatisticController {

    AppointmentStatisticService appointmentStatisticService;


    @GetMapping("/date/{date}")
    ApiResponse<AppointmentStatisticResponse> getStatisticsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Fetching appointment statistics for date: {}", date);
        return ApiResponse.<AppointmentStatisticResponse>builder()
                .result(appointmentStatisticService.getStatisticsByDate(date))
                .build();
    }


    @GetMapping("/range")
    ApiResponse<List<AppointmentStatisticResponse>> getStatisticsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching appointment statistics from {} to {}", startDate, endDate);
        return ApiResponse.<List<AppointmentStatisticResponse>>builder()
                .result(appointmentStatisticService.getStatisticsByDateRange(startDate, endDate))
                .build();
    }


    @GetMapping("/total")
    ApiResponse<Long> getTotalAppointmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching total appointments from {} to {}", startDate, endDate);
        return ApiResponse.<Long>builder()
                .result(appointmentStatisticService.getTotalAppointmentsByDateRange(startDate, endDate))
                .build();
    }


    @GetMapping("/completed")
    ApiResponse<Long> getCompletedAppointmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching completed appointments from {} to {}", startDate, endDate);
        return ApiResponse.<Long>builder()
                .result(appointmentStatisticService.getCompletedAppointmentsByDateRange(startDate, endDate))
                .build();
    }


    @GetMapping("/cancelled")
    ApiResponse<Long> getCancelledAppointmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching cancelled appointments from {} to {}", startDate, endDate);
        return ApiResponse.<Long>builder()
                .result(appointmentStatisticService.getCancelledAppointmentsByDateRange(startDate, endDate))
                .build();
    }


    @GetMapping("/pending")
    ApiResponse<Long> getPendingAppointmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching pending appointments from {} to {}", startDate, endDate);
        return ApiResponse.<Long>builder()
                .result(appointmentStatisticService.getPendingAppointmentsByDateRange(startDate, endDate))
                .build();
    }


    @GetMapping("/cancellation-rate")
    ApiResponse<BigDecimal> getAvgCancellationRate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching average cancellation rate from {} to {}", startDate, endDate);
        return ApiResponse.<BigDecimal>builder()
                .result(appointmentStatisticService.getAvgCancellationRate(startDate, endDate))
                .build();
    }


    @GetMapping("/cancellation-trend")
    ApiResponse<List<AppointmentStatisticResponse>> getCancellationRateTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching cancellation rate trend from {} to {}", startDate, endDate);
        return ApiResponse.<List<AppointmentStatisticResponse>>builder()
                .result(appointmentStatisticService.getCancellationRateTrend(startDate, endDate))
                .build();
    }
}
