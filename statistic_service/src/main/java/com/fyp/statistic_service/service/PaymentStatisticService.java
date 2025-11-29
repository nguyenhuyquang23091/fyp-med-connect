package com.fyp.statistic_service.service;

import com.fyp.statistic_service.dto.response.PaymentStatisticResponse;
import com.fyp.statistic_service.entity.PaymentStatistic;
import com.fyp.statistic_service.mapper.PaymentStatisticMapper;
import com.fyp.statistic_service.repository.PaymentStatisticRepository;
import event.dto.PaymentEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentStatisticService {

    PaymentStatisticRepository paymentStatisticRepository;
    PaymentStatisticMapper paymentStatisticMapper;
    SocketIOEventEmitter socketIOEventEmitter;

    @Transactional
    @CacheEvict(value = {"PAYMENT_STATISTICS_CACHE", "DASHBOARD_SUMMARY_CACHE", "REVENUE_TREND_CACHE"}, allEntries = true)
    public void processPaymentCompletedEvent(PaymentEvent paymentEvent) {
        LocalDate today = LocalDate.now();

        PaymentStatistic statistic = paymentStatisticRepository.findByDate(today)
                .orElseGet(() -> createInitialStatistic(today));

        statistic.setTransactionCount(statistic.getTransactionCount() + 1);
        statistic.setTotalRevenue(statistic.getTotalRevenue().add(paymentEvent.getAmount()));

        updatePaymentMethodBreakdown(statistic, "VNPAY", true);
        updatePaymentStatusBreakdown(statistic, paymentEvent.getPaymentStatus(), true);

        recalculateAverageTransactionAmount(statistic);

        PaymentStatistic savedStatistic = paymentStatisticRepository.save(statistic);
        log.info("Updated payment statistics for date: {} - Payment: {}", today, paymentEvent.getPaymentId());

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("date", today.toString());
        eventData.put("totalRevenue", savedStatistic.getTotalRevenue());
        eventData.put("transactionCount", savedStatistic.getTransactionCount());
        eventData.put("averageTransactionAmount", savedStatistic.getAverageTransactionAmount());

        socketIOEventEmitter.emitPaymentStatisticUpdate(eventData);
    }

    @Transactional
    public void processRefundEvent(String paymentId, BigDecimal refundAmount) {
        LocalDate today = LocalDate.now();

        PaymentStatistic statistic = paymentStatisticRepository.findByDate(today)
                .orElseGet(() -> createInitialStatistic(today));

        statistic.setRefundCount(statistic.getRefundCount() + 1);
        statistic.setRefundedAmount(statistic.getRefundedAmount().add(refundAmount));

        paymentStatisticRepository.save(statistic);
        log.info("Updated refund statistics for date: {} - Payment: {}", today, paymentId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "PAYMENT_STATISTICS_CACHE", key = "#date.toString()")
    public PaymentStatisticResponse getStatisticsByDate(LocalDate date) {
        PaymentStatistic entity = paymentStatisticRepository.findByDate(date)
                .orElseThrow(() -> new RuntimeException("No statistics found for date: " + date));
        return paymentStatisticMapper.toResponse(entity);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "PAYMENT_STATISTICS_CACHE", key = "#startDate.toString() + '_' + #endDate.toString()")
    public List<PaymentStatisticResponse> getStatisticsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<PaymentStatistic> entities = paymentStatisticRepository.findByDateBetweenOrderByDateDesc(startDate, endDate);
        return paymentStatisticMapper.toResponseList(entities);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "DASHBOARD_SUMMARY_CACHE", key = "'total_revenue_' + #startDate.toString() + '_' + #endDate.toString()")
    public BigDecimal getTotalRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
        return paymentStatisticRepository.sumTotalRevenueByDateRange(startDate, endDate);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "PAYMENT_STATISTICS_CACHE", key = "'transaction_count_' + #startDate.toString() + '_' + #endDate.toString()")
    public Long getTransactionCountByDateRange(LocalDate startDate, LocalDate endDate) {
        return paymentStatisticRepository.sumTransactionCountByDateRange(startDate, endDate);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "PAYMENT_STATISTICS_CACHE", key = "'refunded_' + #startDate.toString() + '_' + #endDate.toString()")
    public BigDecimal getRefundedAmountByDateRange(LocalDate startDate, LocalDate endDate) {
        return paymentStatisticRepository.sumRefundedAmountByDateRange(startDate, endDate);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "DASHBOARD_SUMMARY_CACHE", key = "'avg_transaction_amount'")
    public BigDecimal getAvgTransactionAmount(LocalDate startDate, LocalDate endDate) {
        return paymentStatisticRepository.avgTransactionAmountByDateRange(startDate, endDate);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "REVENUE_TREND_CACHE", key = "#startDate.toString() + '_' + #endDate.toString()")
    public List<PaymentStatisticResponse> getRevenueTrend(LocalDate startDate, LocalDate endDate) {
        List<PaymentStatistic> entities = paymentStatisticRepository.findRevenueTrend(startDate, endDate);
        return paymentStatisticMapper.toResponseList(entities);
    }

    private PaymentStatistic createInitialStatistic(LocalDate date) {
        return PaymentStatistic.builder()
                .date(date)
                .totalRevenue(BigDecimal.ZERO)
                .averageTransactionAmount(BigDecimal.ZERO)
                .transactionCount(0L)
                .refundedAmount(BigDecimal.ZERO)
                .refundCount(0L)
                .paymentMethodBreakdown(new HashMap<>())
                .paymentStatusBreakdown(new HashMap<>())
                .build();
    }

    private void updatePaymentMethodBreakdown(PaymentStatistic statistic, String paymentMethod, boolean increment) {
        Map<String, Long> breakdown = statistic.getPaymentMethodBreakdown();
        if (breakdown == null) {
            breakdown = new HashMap<>();
            statistic.setPaymentMethodBreakdown(breakdown);
        }

        Long currentCount = breakdown.getOrDefault(paymentMethod, 0L);
        breakdown.put(paymentMethod, currentCount + (increment ? 1 : -1));
    }

    private void updatePaymentStatusBreakdown(PaymentStatistic statistic, String paymentStatus, boolean increment) {
        if (paymentStatus == null) {
            paymentStatus = "UNKNOWN";
        }

        Map<String, Long> breakdown = statistic.getPaymentStatusBreakdown();
        if (breakdown == null) {
            breakdown = new HashMap<>();
            statistic.setPaymentStatusBreakdown(breakdown);
        }

        Long currentCount = breakdown.getOrDefault(paymentStatus, 0L);
        breakdown.put(paymentStatus, currentCount + (increment ? 1 : -1));
    }

    private void recalculateAverageTransactionAmount(PaymentStatistic statistic) {
        if (statistic.getTransactionCount() > 0) {
            statistic.setAverageTransactionAmount(
                    statistic.getTotalRevenue()
                            .divide(BigDecimal.valueOf(statistic.getTransactionCount()), 2, BigDecimal.ROUND_HALF_UP)
            );
        } else {
            statistic.setAverageTransactionAmount(BigDecimal.ZERO);
        }
    }
}
