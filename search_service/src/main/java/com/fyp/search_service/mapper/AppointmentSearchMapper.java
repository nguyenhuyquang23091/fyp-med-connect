package com.fyp.search_service.mapper;

import com.fyp.search_service.dto.response.AppointmentResponse;
import com.fyp.search_service.entity.AppointmentEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AppointmentSearchMapper {

    AppointmentResponse toAppointmentResponse(AppointmentEntity appointmentEntity);
}
