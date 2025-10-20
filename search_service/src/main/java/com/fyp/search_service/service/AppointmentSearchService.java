package com.fyp.search_service.service;


import com.fyp.search_service.entity.AppointmentEntity;
import com.fyp.search_service.mapper.AppointmentCdcMapper;
import com.fyp.search_service.repository.AppointmentSearchRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentSearchService {
    AppointmentSearchRepository appointmentSearchRepository;
    AppointmentCdcMapper appointmentCdcMapper;

    public void indexAppointment(Map<String, Object> appointmentData) {
        try {
            AppointmentEntity appointment = appointmentCdcMapper.toAppointmentEntity(appointmentData);
            appointmentSearchRepository.save(appointment);
            log.info("Successfully indexed appointment: {}", appointmentData.get("id"));
        } catch (Exception e) {
            log.error("Error indexing appointment: {}", appointmentData.get("id"), e);
        }
    }


    public void deleteAppointment(String appointmentId) {
        try {
            appointmentSearchRepository.deleteById(appointmentId);
            log.info("Successfully deleted appointment: {}", appointmentId);
        } catch (Exception e) {
            log.error("Error deleting appointment: {}", appointmentId, e);
        }
    }
}
