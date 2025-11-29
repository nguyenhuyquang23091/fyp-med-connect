package com.fyp.statistic_service.mapper;

import com.fyp.statistic_service.dto.response.PaymentStatisticResponse;
import com.fyp.statistic_service.entity.PaymentStatistic;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentStatisticMapper {
    PaymentStatisticResponse toResponse(PaymentStatistic entity);
    
    List<PaymentStatisticResponse> toResponseList(List<PaymentStatistic> entities);
}

