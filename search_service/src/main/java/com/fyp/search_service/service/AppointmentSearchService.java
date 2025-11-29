package com.fyp.search_service.service;


import com.fyp.search_service.dto.request.AppointmentSearchFilter;
import com.fyp.search_service.dto.response.AppointmentResponse;
import com.fyp.search_service.dto.response.PageResponse;
import com.fyp.search_service.dto.response.SearchSuggestion;
import com.fyp.search_service.entity.AppointmentEntity;
import com.fyp.search_service.exceptions.AppException;
import com.fyp.search_service.exceptions.ErrorCode;
import com.fyp.search_service.mapper.AppointmentCdcMapper;
import com.fyp.search_service.repository.AppointmentSearchRepository;
import com.fyp.search_service.search.ElasticSearchProxy;
import com.fyp.search_service.utils.SearchServiceUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentSearchService {
    AppointmentSearchRepository appointmentSearchRepository;
    AppointmentCdcMapper appointmentCdcMapper;
    ElasticSearchProxy elasticSearchProxy;

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
            log.error("Error deleting appointment from Elasticsearch: appointmentId={}", appointmentId, e);
            throw new AppException(ErrorCode.ELASTICSEARCH_DELETE_ERROR);
        }
    }

    public PageResponse<AppointmentResponse> searchAppointments(AppointmentSearchFilter filter) {
        log.debug("Searching appointments with filter: {}", filter);

        String currentUserId = SearchServiceUtil.getCurrentUserId();
        String userRole = determineUserRole();

        switch (userRole) {
            case "ADMIN" -> log.debug("Admin access - no filtering applied");
            case "PATIENT" -> {
                filter.setUserId(currentUserId);
                log.debug("Applied patient filter: userId={}", currentUserId);
            }
            case "DOCTOR" -> {
                filter.setDoctorId(currentUserId);
                log.debug("Applied doctor filter: doctorId={}", currentUserId);
            }
            default -> log.warn("Unknown role, no filtering applied");
        }

        return elasticSearchProxy.searchAppointments(filter);
    }

    private String determineUserRole() {
        if (SearchServiceUtil.isAdmin()) return "ADMIN";
        if (SearchServiceUtil.isPatient()) return "PATIENT";
        if (SearchServiceUtil.isDoctor()) return "DOCTOR";
        return "UNKNOWN";
    }

    public List<SearchSuggestion> getAppointmentSuggestions(String term, int limit) {
        log.debug("Getting appointment suggestions for term: {}, limit: {}", term, limit);

        String currentUserId = SearchServiceUtil.getCurrentUserId();
        String userRole = determineUserRole();

        String userId = null;
        String doctorId = null;

        switch (userRole) {
            case "ADMIN" -> log.debug("Admin access - no filtering applied for suggestions");
            case "PATIENT" -> {
                userId = currentUserId;
                log.debug("Applied patient filter for suggestions: userId={}", currentUserId);
            }
            case "DOCTOR" -> {
                doctorId = currentUserId;
                log.debug("Applied doctor filter for suggestions: doctorId={}", currentUserId);
            }
            default -> log.warn("Unknown role, no filtering applied for suggestions");
        }

        return elasticSearchProxy.getAppointmentSuggestions(term, limit, userId, doctorId);
    }
}
