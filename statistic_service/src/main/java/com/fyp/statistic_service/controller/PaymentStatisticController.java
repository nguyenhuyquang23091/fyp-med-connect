package com.fyp.statistic_service.controller;

import com.fyp.statistic_service.dto.request.ApiResponse;
import com.fyp.statistic_service.dto.response.PaymentStatisticResponse;
import com.fyp.statistic_service.service.PaymentStatisticService;
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
@RequestMapping("/payment-statistics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PaymentStatisticController {

    PaymentStatisticService paymentStatisticService;


    @GetMapping("/date/{date}")
    ApiResponse<PaymentStatisticResponse> getStatisticsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Fetching payment statistics for date: {}", date);
        return ApiResponse.<PaymentStatisticResponse>builder()
                .result(paymentStatisticService.getStatisticsByDate(date))
                .build();
    }


    @GetMapping("/range")
    ApiResponse<List<PaymentStatisticResponse>> getStatisticsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching payment statistics from {} to {}", startDate, endDate);
        return ApiResponse.<List<PaymentStatisticResponse>>builder()
                .result(paymentStatisticService.getStatisticsByDateRange(startDate, endDate))
                .build();
    }


    @GetMapping("/revenue")
    ApiResponse<BigDecimal> getTotalRevenueByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching total revenue from {} to {}", startDate, endDate);
        return ApiResponse.<BigDecimal>builder()
                .result(paymentStatisticService.getTotalRevenueByDateRange(startDate, endDate))
                .build();
    }


    @GetMapping("/transactions")
    ApiResponse<Long> getTransactionCountByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching transaction count from {} to {}", startDate, endDate);
        return ApiResponse.<Long>builder()
                .result(paymentStatisticService.getTransactionCountByDateRange(startDate, endDate))
                .build();
    }


    @GetMapping("/refunds")
    ApiResponse<BigDecimal> getRefundedAmountByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching refunded amount from {} to {}", startDate, endDate);
        return ApiResponse.<BigDecimal>builder()
                .result(paymentStatisticService.getRefundedAmountByDateRange(startDate, endDate))
                .build();
    }


    @GetMapping("/average-transaction")
    ApiResponse<BigDecimal> getAvgTransactionAmount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching average transaction amount from {} to {}", startDate, endDate);
        return ApiResponse.<BigDecimal>builder()
                .result(paymentStatisticService.getAvgTransactionAmount(startDate, endDate))
                .build();
    }

    @GetMapping("/revenue-trend")
    ApiResponse<List<PaymentStatisticResponse>> getRevenueTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching revenue trend from {} to {}", startDate, endDate);
        return ApiResponse.<List<PaymentStatisticResponse>>builder()
                .result(paymentStatisticService.getRevenueTrend(startDate, endDate))
                .build();
    }
}
