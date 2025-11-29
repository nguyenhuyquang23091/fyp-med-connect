package com.fyp.statistic_service.service;


import com.fyp.statistic_service.dto.response.StatisticEventLogsResponse;
import com.fyp.statistic_service.mapper.StatisticEventLogsMapper;
import com.fyp.statistic_service.repository.StatisticEventLogRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatisticEventLogService {
    StatisticEventLogRepository statisticEventLogRepository;
    StatisticEventLogsMapper statisticEventLogsMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public boolean eventExists(String eventId){
        return statisticEventLogRepository.existsByEventId(eventId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Optional<StatisticEventLogsResponse> findByEventId(String eventId){
        return statisticEventLogRepository.findByEventId(eventId)
                .map(statisticEventLogsMapper::toResponse);
    }
}
