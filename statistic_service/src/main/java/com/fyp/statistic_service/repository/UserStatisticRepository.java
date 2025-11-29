package com.fyp.statistic_service.repository;


import com.fyp.statistic_service.entity.UserStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserStatisticRepository extends JpaRepository<UserStatistic, String> {


    Optional<UserStatistic> findByDate(LocalDate date);


    List<UserStatistic> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate);


    List<UserStatistic> findTop10ByOrderByDateDesc();


    @Query("SELECT COALESCE(SUM(us.newUsers), 0) FROM UserStatistic us WHERE us.date BETWEEN :startDate AND :endDate")
    Long sumNewUsersByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    @Query("SELECT COALESCE(AVG(us.activeUsers), 0) FROM UserStatistic us WHERE us.date BETWEEN :startDate AND :endDate")
    Double avgActiveUsersByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    @Query("SELECT us.totalUsers FROM UserStatistic us ORDER BY us.date DESC LIMIT 1")
    Optional<Long> findLatestTotalUsers();


    @Query("SELECT us FROM UserStatistic us WHERE us.date BETWEEN :startDate AND :endDate ORDER BY us.date ASC")
    List<UserStatistic> findUserGrowthTrend(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    boolean existsByDate(LocalDate date);

}
