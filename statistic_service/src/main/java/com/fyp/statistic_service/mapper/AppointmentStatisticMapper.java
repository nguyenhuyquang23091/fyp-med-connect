package com.fyp.statistic_service.mapper;

import com.fyp.statistic_service.dto.response.AppointmentStatisticResponse;
import com.fyp.statistic_service.entity.AppointmentStatistic;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AppointmentStatisticMapper {
    AppointmentStatisticResponse toResponse(AppointmentStatistic entity);
    
    List<AppointmentStatisticResponse> toResponseList(List<AppointmentStatistic> entities);
}

