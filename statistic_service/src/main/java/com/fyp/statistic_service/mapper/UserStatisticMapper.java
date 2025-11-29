package com.fyp.statistic_service.mapper;

import com.fyp.statistic_service.dto.response.UserStatisticResponse;
import com.fyp.statistic_service.entity.UserStatistic;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserStatisticMapper {
    UserStatisticResponse toResponse(UserStatistic entity);
    
    List<UserStatisticResponse> toResponseList(List<UserStatistic> entities);
}

