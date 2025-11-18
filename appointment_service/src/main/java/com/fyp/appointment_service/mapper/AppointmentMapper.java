package com.fyp.appointment_service.mapper;

import com.fyp.appointment_service.dto.request.AppointmentRequest;
import com.fyp.appointment_service.dto.request.AppointmentUpdateRequest;
import com.fyp.appointment_service.dto.request.DoctorAppointmentUpdate;
import com.fyp.appointment_service.dto.request.PatientAppointmentUpdate;
import com.fyp.appointment_service.dto.response.AppointmentResponse;
import com.fyp.appointment_service.entity.AppointmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {
    AppointmentEntity toAppointmentEntity(AppointmentRequest request);
    AppointmentResponse toAppointmentRespone(AppointmentEntity appointmentEntity);


    void updateAppointment(@MappingTarget AppointmentEntity appointmentEntity, AppointmentUpdateRequest appointmentUpdateRequest);


}
