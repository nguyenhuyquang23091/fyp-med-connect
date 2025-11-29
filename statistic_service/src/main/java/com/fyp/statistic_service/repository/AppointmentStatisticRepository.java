package com.fyp.statistic_service.repository;

import com.fyp.statistic_service.entity.AppointmentStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentStatisticRepository extends JpaRepository<AppointmentStatistic, String> {


    Optional<AppointmentStatistic> findByDate(LocalDate date);


    List<AppointmentStatistic> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate);

    List<AppointmentStatistic> findTop10ByOrderByDateDesc();


    @Query("SELECT COALESCE(SUM(a.totalAppointments), 0) FROM AppointmentStatistic a WHERE a.date BETWEEN :startDate AND :endDate")
    Long sumTotalAppointmentsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(a.completedAppointments), 0) FROM AppointmentStatistic a WHERE a.date BETWEEN :startDate AND :endDate")
    Long sumCompletedAppointmentsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    @Query("SELECT COALESCE(SUM(a.cancelledAppointments), 0) FROM AppointmentStatistic a WHERE a.date BETWEEN :startDate AND :endDate")
    Long sumCancelledAppointmentsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    @Query("SELECT COALESCE(SUM(a.pendingAppointments), 0) FROM AppointmentStatistic a WHERE a.date BETWEEN :startDate AND :endDate")
    Long sumPendingAppointmentsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    @Query("SELECT COALESCE(AVG(a.cancellationRate), 0) FROM AppointmentStatistic a WHERE a.date BETWEEN :startDate AND :endDate")
    BigDecimal avgCancellationRateByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    @Query("SELECT a FROM AppointmentStatistic a WHERE a.date BETWEEN :startDate AND :endDate ORDER BY a.date ASC")
    List<AppointmentStatistic> findCancellationRateTrend(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    boolean existsByDate(LocalDate date);

}
