package com.fyp.statistic_service.mapper;

import com.fyp.statistic_service.dto.response.StatisticEventLogsResponse;
import com.fyp.statistic_service.entity.StatisticEventLogs;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StatisticEventLogsMapper {
    StatisticEventLogsResponse toResponse(StatisticEventLogs entity);
}

