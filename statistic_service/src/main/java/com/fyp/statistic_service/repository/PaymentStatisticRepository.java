package com.fyp.statistic_service.repository;


import com.fyp.statistic_service.entity.PaymentStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentStatisticRepository extends JpaRepository<PaymentStatistic, String> {


    Optional<PaymentStatistic> findByDate(LocalDate date);


    List<PaymentStatistic> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate);


    List<PaymentStatistic> findTop10ByOrderByDateDesc();


    @Query("SELECT COALESCE(SUM(p.totalRevenue), 0) FROM PaymentStatistic p WHERE p.date BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalRevenueByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    @Query("SELECT COALESCE(SUM(p.transactionCount), 0) FROM PaymentStatistic p WHERE p.date BETWEEN :startDate AND :endDate")
    Long sumTransactionCountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(p.refundedAmount), 0) FROM PaymentStatistic p WHERE p.date BETWEEN :startDate AND :endDate")
    BigDecimal sumRefundedAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    @Query("SELECT COALESCE(SUM(p.refundCount), 0) FROM PaymentStatistic p WHERE p.date BETWEEN :startDate AND :endDate")
    Long sumRefundCountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    @Query("SELECT COALESCE(AVG(p.averageTransactionAmount), 0) FROM PaymentStatistic p WHERE p.date BETWEEN :startDate AND :endDate")
    BigDecimal avgTransactionAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    @Query("SELECT p FROM PaymentStatistic p WHERE p.date BETWEEN :startDate AND :endDate ORDER BY p.date ASC")
    List<PaymentStatistic> findRevenueTrend(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    boolean existsByDate(LocalDate date);

}
