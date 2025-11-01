package com.fyp.search_service.service;


import com.fyp.search_service.entity.AppointmentEntity;
import com.fyp.search_service.exceptions.AppException;
import com.fyp.search_service.exceptions.ErrorCode;
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

    public void saveAppointment(Map<String, Object> appointmentData) {
        try {
            String appointmentId = appointmentData.get("id") != null ? appointmentData.get("id").toString() : "unknown";
            log.debug("Indexing appointment in Elasticsearch: appointmentId={}", appointmentId);

            AppointmentEntity appointment = appointmentCdcMapper.toAppointmentEntity(appointmentData);
            appointmentSearchRepository.save(appointment);

            log.info("Successfully indexed appointment: appointmentId={}", appointmentId);
        } catch (IllegalArgumentException e) {
            // Reason: Mapping errors indicate invalid or malformed CDC event data
            log.error("Failed to deserialize appointment data: {}", appointmentData.get("id"), e);
            throw new AppException(ErrorCode.CDC_EVENT_DESERIALIZATION_ERROR);
        } catch (Exception e) {
            // Reason: Repository save failures indicate Elasticsearch indexing issues
            log.error("Error indexing appointment in Elasticsearch: appointmentId={}", appointmentData.get("id"), e);
            throw new AppException(ErrorCode.ELASTICSEARCH_INDEX_ERROR);
        }
    }

    public void deleteAppointment(String appointmentId) {
        try {
            log.debug("Deleting appointment from Elasticsearch: appointmentId={}", appointmentId);

            appointmentSearchRepository.deleteById(appointmentId);

            log.info("Successfully deleted appointment from Elasticsearch: appointmentId={}", appointmentId);
        } catch (Exception e) {
            // Reason: Repository delete failures indicate Elasticsearch deletion issues
            log.error("Error deleting appointment from Elasticsearch: appointmentId={}", appointmentId, e);
            throw new AppException(ErrorCode.ELASTICSEARCH_DELETE_ERROR);
        }
    }
}
