package com.fyp.statistic_service.scheduler;

import com.fyp.statistic_service.entity.AppointmentStatistic;
import com.fyp.statistic_service.entity.PaymentStatistic;
import com.fyp.statistic_service.entity.UserStatistic;
import com.fyp.statistic_service.repository.AppointmentStatisticRepository;
import com.fyp.statistic_service.repository.PaymentStatisticRepository;
import com.fyp.statistic_service.repository.UserStatisticRepository;
import com.fyp.statistic_service.service.AppointmentStatisticService;
import com.fyp.statistic_service.service.PaymentStatisticService;
import com.fyp.statistic_service.service.UserStatisticService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DailyStatisticsScheduler {

    UserStatisticRepository userStatisticRepository;
    AppointmentStatisticRepository appointmentStatisticRepository;
    PaymentStatisticRepository paymentStatisticRepository;
    JdbcTemplate jdbcTemplate;

    UserStatisticService userStatisticService;
    AppointmentStatisticService appointmentStatisticService;
    PaymentStatisticService paymentStatisticService;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void aggregateDailyUserStatistics() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Starting daily user statistics aggregation for date: {}", yesterday);

        try {
            String countUsersSql = "SELECT COUNT(*) FROM users WHERE DATE(created_at) = ?";
            Long newUsersCount = jdbcTemplate.queryForObject(countUsersSql, Long.class, yesterday);

            String totalUsersSql = "SELECT COUNT(*) FROM users WHERE DATE(created_at) <= ?";
            Long totalUsersCount = jdbcTemplate.queryForObject(totalUsersSql, Long.class, yesterday);

            String doctorCountSql = "SELECT COUNT(*) FROM users u JOIN user_roles ur ON u.id = ur.user_id WHERE ur.role_name = 'DOCTOR' AND DATE(u.created_at) <= ?";
            Long doctorCount = jdbcTemplate.queryForObject(doctorCountSql, Long.class, yesterday);

            String patientCountSql = "SELECT COUNT(*) FROM users u JOIN user_roles ur ON u.id = ur.user_id WHERE ur.role_name = 'PATIENT' AND DATE(u.created_at) <= ?";
            Long patientCount = jdbcTemplate.queryForObject(patientCountSql, Long.class, yesterday);

            UserStatistic statistic = userStatisticRepository.findByDate(yesterday)
                    .orElse(UserStatistic.builder()
                            .date(yesterday)
                            .newUsers(0L)
                            .totalUsers(0L)
                            .activeUsers(0L)
                            .doctorCount(0L)
                            .patientCount(0L)
                            .adminCount(0L)
                            .build());

            statistic.setNewUsers(newUsersCount != null ? newUsersCount : 0L);
            statistic.setTotalUsers(totalUsersCount != null ? totalUsersCount : 0L);
            statistic.setDoctorCount(doctorCount != null ? doctorCount : 0L);
            statistic.setPatientCount(patientCount != null ? patientCount : 0L);

            userStatisticRepository.save(statistic);
            log.info("Successfully aggregated user statistics for {}: {} new users, {} total users",
                    yesterday, newUsersCount, totalUsersCount);

        } catch (Exception e) {
            log.error("Failed to aggregate daily user statistics for date: {}", yesterday, e);
        }
    }

    @Scheduled(cron = "0 5 0 * * ?")
    @Transactional
    public void aggregateDailyAppointmentStatistics() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Starting daily appointment statistics aggregation for date: {}", yesterday);

        try {
            String totalSql = "SELECT COUNT(*) FROM appointments WHERE DATE(created_at) = ?";
            Long totalAppointments = jdbcTemplate.queryForObject(totalSql, Long.class, yesterday);

            String completedSql = "SELECT COUNT(*) FROM appointments WHERE DATE(created_at) = ? AND status = 'COMPLETED'";
            Long completedAppointments = jdbcTemplate.queryForObject(completedSql, Long.class, yesterday);

            String cancelledSql = "SELECT COUNT(*) FROM appointments WHERE DATE(created_at) = ? AND status = 'CANCELLED'";
            Long cancelledAppointments = jdbcTemplate.queryForObject(cancelledSql, Long.class, yesterday);

            String pendingSql = "SELECT COUNT(*) FROM appointments WHERE DATE(created_at) = ? AND status = 'PENDING'";
            Long pendingAppointments = jdbcTemplate.queryForObject(pendingSql, Long.class, yesterday);

            AppointmentStatistic statistic = appointmentStatisticRepository.findByDate(yesterday)
                    .orElse(AppointmentStatistic.builder()
                            .date(yesterday)
                            .totalAppointments(0L)
                            .completedAppointments(0L)
                            .cancelledAppointments(0L)
                            .pendingAppointments(0L)
                            .inProgressAppointments(0L)
                            .cancellationRate(BigDecimal.ZERO)
                            .consultationTypeBreakdown(new HashMap<>())
                            .departmentBreakdown(new HashMap<>())
                            .build());

            statistic.setTotalAppointments(totalAppointments != null ? totalAppointments : 0L);
            statistic.setCompletedAppointments(completedAppointments != null ? completedAppointments : 0L);
            statistic.setCancelledAppointments(cancelledAppointments != null ? cancelledAppointments : 0L);
            statistic.setPendingAppointments(pendingAppointments != null ? pendingAppointments : 0L);

            if (totalAppointments != null && totalAppointments > 0) {
                statistic.setCancellationRate(
                        BigDecimal.valueOf(cancelledAppointments != null ? cancelledAppointments : 0L)
                                .divide(BigDecimal.valueOf(totalAppointments), 4, BigDecimal.ROUND_HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                );
            }

            appointmentStatisticRepository.save(statistic);
            log.info("Successfully aggregated appointment statistics for {}: {} total, {} completed, {} cancelled",
                    yesterday, totalAppointments, completedAppointments, cancelledAppointments);

        } catch (Exception e) {
            log.error("Failed to aggregate daily appointment statistics for date: {}", yesterday, e);
        }
    }

    @Scheduled(cron = "0 10 0 * * ?")
    @Transactional
    public void aggregateDailyPaymentStatistics() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Starting daily payment statistics aggregation for date: {}", yesterday);

        try {
            String revenueSql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE DATE(payment_date) = ? AND payment_status = 'PAID'";
            BigDecimal totalRevenue = jdbcTemplate.queryForObject(revenueSql, BigDecimal.class, yesterday);

            String countSql = "SELECT COUNT(*) FROM payments WHERE DATE(payment_date) = ?";
            Long transactionCount = jdbcTemplate.queryForObject(countSql, Long.class, yesterday);

            String refundAmountSql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE DATE(payment_date) = ? AND payment_status = 'REFUNDED'";
            BigDecimal refundedAmount = jdbcTemplate.queryForObject(refundAmountSql, BigDecimal.class, yesterday);

            String refundCountSql = "SELECT COUNT(*) FROM payments WHERE DATE(payment_date) = ? AND payment_status = 'REFUNDED'";
            Long refundCount = jdbcTemplate.queryForObject(refundCountSql, Long.class, yesterday);

            PaymentStatistic statistic = paymentStatisticRepository.findByDate(yesterday)
                    .orElse(PaymentStatistic.builder()
                            .date(yesterday)
                            .totalRevenue(BigDecimal.ZERO)
                            .averageTransactionAmount(BigDecimal.ZERO)
                            .transactionCount(0L)
                            .refundedAmount(BigDecimal.ZERO)
                            .refundCount(0L)
                            .paymentMethodBreakdown(new HashMap<>())
                            .paymentStatusBreakdown(new HashMap<>())
                            .build());

            statistic.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
            statistic.setTransactionCount(transactionCount != null ? transactionCount : 0L);
            statistic.setRefundedAmount(refundedAmount != null ? refundedAmount : BigDecimal.ZERO);
            statistic.setRefundCount(refundCount != null ? refundCount : 0L);

            if (transactionCount != null && transactionCount > 0 && totalRevenue != null) {
                statistic.setAverageTransactionAmount(
                        totalRevenue.divide(BigDecimal.valueOf(transactionCount), 2, BigDecimal.ROUND_HALF_UP)
                );
            }

            paymentStatisticRepository.save(statistic);
            log.info("Successfully aggregated payment statistics for {}: {} revenue, {} transactions",
                    yesterday, totalRevenue, transactionCount);

        } catch (Exception e) {
            log.error("Failed to aggregate daily payment statistics for date: {}", yesterday, e);
        }
    }

    @Scheduled(cron = "0 15 0 * * ?")
    @Transactional
    public void verifyDataIntegrity() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Starting data integrity verification for date: {}", yesterday);

        try {
            boolean userStatsExists = userStatisticRepository.existsByDate(yesterday);
            boolean appointmentStatsExists = appointmentStatisticRepository.existsByDate(yesterday);
            boolean paymentStatsExists = paymentStatisticRepository.existsByDate(yesterday);

            if (!userStatsExists) {
                log.warn("Missing user statistics for date: {}, triggering aggregation", yesterday);
                aggregateDailyUserStatistics();
            }

            if (!appointmentStatsExists) {
                log.warn("Missing appointment statistics for date: {}, triggering aggregation", yesterday);
                aggregateDailyAppointmentStatistics();
            }

            if (!paymentStatsExists) {
                log.warn("Missing payment statistics for date: {}, triggering aggregation", yesterday);
                aggregateDailyPaymentStatistics();
            }

            log.info("Data integrity verification completed for date: {}", yesterday);

        } catch (Exception e) {
            log.error("Failed to verify data integrity for date: {}", yesterday, e);
        }
    }

    @Scheduled(cron = "0 */10 * * * ?")
    public void warmDashboardCache() {
        log.debug("Starting cache warming for dashboard metrics");

        try {
            LocalDate today = LocalDate.now();
            LocalDate weekStart = today.minusDays(7);
            LocalDate monthStart = today.minusDays(30);

            userStatisticService.getLatestTotalUsers();
            userStatisticService.getWeeklyActiveUsers();
            userStatisticService.getMonthlyActiveUsers();

            appointmentStatisticService.getTotalAppointmentsByDateRange(today, today);
            appointmentStatisticService.getTotalAppointmentsByDateRange(weekStart, today);
            appointmentStatisticService.getTotalAppointmentsByDateRange(monthStart, today);
            appointmentStatisticService.getAvgCancellationRate(weekStart, today);

            paymentStatisticService.getTotalRevenueByDateRange(today, today);
            paymentStatisticService.getTotalRevenueByDateRange(weekStart, today);
            paymentStatisticService.getTotalRevenueByDateRange(monthStart, today);
            paymentStatisticService.getAvgTransactionAmount(weekStart, today);

            log.debug("Dashboard cache warming completed successfully");

        } catch (Exception e) {
            log.error("Failed to warm cache for dashboard metrics", e);
        }
    }

    @Scheduled(cron = "0 0 7 * * ?")
    public void warmMorningDashboardCache() {
        log.info("Starting morning cache warming for dashboard");

        try {
            warmDashboardCache();

            LocalDate endDate = LocalDate.now();
            LocalDate startDate30Days = endDate.minusDays(30);

            userStatisticService.getUserGrowthTrend(startDate30Days, endDate);
            appointmentStatisticService.getCancellationRateTrend(startDate30Days, endDate);
            paymentStatisticService.getRevenueTrend(startDate30Days, endDate);

            log.info("Morning cache warming completed - Dashboard ready for users");

        } catch (Exception e) {
            log.error("Failed to warm morning cache", e);
        }
    }
}
