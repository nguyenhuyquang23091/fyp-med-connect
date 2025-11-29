package com.fyp.statistic_service.controller;

import com.fyp.statistic_service.dto.request.ApiResponse;
import com.fyp.statistic_service.dto.response.UserStatisticResponse;
import com.fyp.statistic_service.service.UserStatisticService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/user-statistics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserStatisticController {

    UserStatisticService userStatisticService;


    @GetMapping("/date/{date}")
    ApiResponse<UserStatisticResponse> getStatisticsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Fetching user statistics for date: {}", date);
        return ApiResponse.<UserStatisticResponse>builder()
                .result(userStatisticService.getStatisticsByDate(date))
                .build();
    }


    @GetMapping("/range")
    ApiResponse<List<UserStatisticResponse>> getStatisticsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching user statistics from {} to {}", startDate, endDate);
        return ApiResponse.<List<UserStatisticResponse>>builder()
                .result(userStatisticService.getStatisticsByDateRange(startDate, endDate))
                .build();
    }

    @GetMapping("/daily-active/{date}")
    ApiResponse<Long> getDailyActiveUsers(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Fetching daily active users for date: {}", date);
        return ApiResponse.<Long>builder()
                .result(userStatisticService.getDailyActiveUsers(date))
                .build();
    }


    @GetMapping("/weekly-active")
    ApiResponse<Double> getWeeklyActiveUsers() {
        log.info("Fetching weekly active users");
        return ApiResponse.<Double>builder()
                .result(userStatisticService.getWeeklyActiveUsers())
                .build();
    }

    @GetMapping("/monthly-active")
    ApiResponse<Double> getMonthlyActiveUsers() {
        log.info("Fetching monthly active users");
        return ApiResponse.<Double>builder()
                .result(userStatisticService.getMonthlyActiveUsers())
                .build();
    }


    @GetMapping("/growth-trend")
    ApiResponse<List<UserStatisticResponse>> getUserGrowthTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching user growth trend from {} to {}", startDate, endDate);
        return ApiResponse.<List<UserStatisticResponse>>builder()
                .result(userStatisticService.getUserGrowthTrend(startDate, endDate))
                .build();
    }


    @GetMapping("/new-users")
    ApiResponse<Long> getNewUsersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching new users from {} to {}", startDate, endDate);
        return ApiResponse.<Long>builder()
                .result(userStatisticService.getNewUsersByDateRange(startDate, endDate))
                .build();
    }

    @GetMapping("/total")
    ApiResponse<Long> getLatestTotalUsers() {
        log.info("Fetching latest total users");
        return ApiResponse.<Long>builder()
                .result(userStatisticService.getLatestTotalUsers())
                .build();
    }
}
