package com.fyp.statistic_service.repository;

import com.fyp.statistic_service.entity.StatisticEventLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatisticEventLogRepository extends JpaRepository<StatisticEventLogs, String> {

    boolean existsByEventId(String eventId);

    Optional<StatisticEventLogs> findByEventId(String eventId);

}
