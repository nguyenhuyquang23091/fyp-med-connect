package com.fyp.search_service.service;

import com.fyp.search_service.dto.request.SearchFilter;
import com.fyp.search_service.dto.response.DoctorProfileResponse;
import com.fyp.search_service.dto.response.PageResponse;
import com.fyp.search_service.dto.response.SearchSuggestion;
import com.fyp.search_service.entity.DoctorProfile;
import com.fyp.search_service.exceptions.AppException;
import com.fyp.search_service.exceptions.ErrorCode;
import com.fyp.search_service.mapper.DoctorProfileCdcMapper;
import com.fyp.search_service.mapper.DoctorProfileSearchMapper;
import com.fyp.search_service.repository.DoctorProfileSearchRepository;
import com.fyp.search_service.search.ElasticSearchProxy;
import com.fyp.search_service.service.base.NestedEntityHandler;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.print.Doc;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.fyp.search_service.constant.PredefinedType.*;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorProfileSearchService {

    DoctorProfileSearchRepository doctorProfileRepository;
    DoctorProfileCdcMapper doctorProfileCdcMapper;
    Map<String, NestedEntityHandler<?>> entityHandlers = new HashMap<>();
    DoctorProfileSearchMapper doctorProfileSearchMapper;
    ElasticSearchProxy elasticSearchProxy;


    @PostConstruct
    private void init() {

        entityHandlers.put(SERVICE, new ServiceNestedEntityHandler(doctorProfileRepository, doctorProfileCdcMapper));
        entityHandlers.put(SPECIALTY, new SpecialtyNestedEntityHandler(doctorProfileRepository, doctorProfileCdcMapper));
        entityHandlers.put(EXPERIENCE, new ExperienceNestedEntityHandler(doctorProfileRepository, doctorProfileCdcMapper));

    }
    public void handleDoctorProfileCdcEvent(String operation, String entityType,
                                             Map<String, Object> beforeData, Map<String, Object> afterData,
                                             String doctorProfileId, String userId) {
        try {
            log.debug("Processing doctor profile CDC event - operation: {}, entityType: {}, profileId: {}",
                    operation, entityType, doctorProfileId);

            // Handle base PROFILE entity type separately (not a nested entity)
            if (PROFILE.equals(entityType)) {
                handleProfileEvent(operation, beforeData, afterData, doctorProfileId, userId);
                return;
            }

            // Handle nested entities (SERVICE, SPECIALTY, EXPERIENCE)
            NestedEntityHandler<?> handler = entityHandlers.get(entityType);
            if (handler != null) {
                handleEvent(handler, operation, beforeData, afterData, doctorProfileId, userId);
            } else {
                // Reason: Unknown entity types indicate invalid CDC events or misconfiguration
                log.error("Unknown entity type received: {}", entityType);
                throw new AppException(ErrorCode.CDC_INVALID_ENTITY_TYPE);
            }

        } catch (AppException e) {
            // Reason: Re-throw AppExceptions to preserve error context
            throw e;
        } catch (Exception e) {
            // Reason: Wrap unexpected exceptions in CDC processing error
            log.error("Error processing doctor profile CDC event - profileId: {}, entityType: {}, operation: {}",
                    doctorProfileId, entityType, operation, e);
            throw new AppException(ErrorCode.CDC_EVENT_PROCESSING_ERROR);
        }
    }


    private void handleProfileEvent(String operation, Map<String, Object> beforeData,
                                      Map<String, Object> afterData, String doctorProfileId, String userId) {
        switch (operation) {
            case "c", "r" -> createOrUpdateProfile(afterData, doctorProfileId, userId);
            case "u" -> updateProfile(afterData, doctorProfileId);
            case "d" -> deleteProfile(doctorProfileId);
            default -> log.warn("Unknown operation for PROFILE: {}", operation);
        }
    }

    private void createOrUpdateProfile(Map<String, Object> data, String doctorProfileId, String userId) {
        DoctorProfile profile = doctorProfileCdcMapper.toProfile(data);

        // Reason: If profile already exists, preserve nested entities and user profile fields
        // that are not included in PROFILE CDC events (firstName, lastName, email, avatar)
        // Languages ARE included in CDC events, so only preserve if CDC data is null

        doctorProfileRepository.findById(doctorProfileId).ifPresent(existing -> {
            profile.setServices(existing.getServices());
            profile.setSpecialties(existing.getSpecialties());
            profile.setExperiences(existing.getExperiences());

            // Only preserve languages if CDC data didn't include it
            if (profile.getLanguages() == null && existing.getLanguages() != null) {
                profile.setLanguages(existing.getLanguages());
            }

            profile.setFirstName(existing.getFirstName());
            profile.setLastName(existing.getLastName());
            profile.setEmail(existing.getEmail());
            profile.setAvatar(existing.getAvatar());
        });

        doctorProfileRepository.save(profile);
        log.info("Created/Updated doctor profile in Elasticsearch - profileId: {}, userId: {}",
                 doctorProfileId, userId);
    }


    private void updateProfile(Map<String, Object> data, String doctorProfileId) {
        doctorProfileRepository.findById(doctorProfileId).ifPresentOrElse(existing -> {
            DoctorProfile updated = doctorProfileCdcMapper.toProfile(data);

            // Preserve nested entities and user profile fields
            updated.setServices(existing.getServices());
            updated.setSpecialties(existing.getSpecialties());
            updated.setExperiences(existing.getExperiences());

            // Only preserve languages if CDC data didn't include it
            if (updated.getLanguages() == null && existing.getLanguages() != null) {
                updated.setLanguages(existing.getLanguages());
            }

            updated.setFirstName(existing.getFirstName());
            updated.setLastName(existing.getLastName());
            updated.setEmail(existing.getEmail());
            updated.setAvatar(existing.getAvatar());



            doctorProfileRepository.save(updated);
            log.info("Updated doctor profile in Elasticsearch - profileId: {}", doctorProfileId);
        }, () -> {
            log.warn("Cannot update non-existent doctor profile - profileId: {}", doctorProfileId);
        });
    }

    private void deleteProfile(String doctorProfileId) {
        doctorProfileRepository.deleteById(doctorProfileId);
        log.info("Deleted doctor profile from Elasticsearch - profileId: {}", doctorProfileId);
    }

    private <T> void handleEvent(NestedEntityHandler<T> handler, String operation, Map<String, Object> beforeData,
                                 Map<String, Object> afterData, String doctorProfileId, String userId) {
        switch (operation) {
            case "c", "r" -> handler.add(afterData, doctorProfileId, userId);
            case "u" -> handler.update(beforeData, afterData, doctorProfileId);
            case "d" -> handler.delete(beforeData, doctorProfileId);
            default -> log.warn("Unknown operation for {}: {}", handler.getEntityName(), operation);
        }
    }

    private static class ServiceNestedEntityHandler extends NestedEntityHandler<DoctorProfile.ServiceInfo> {
        public ServiceNestedEntityHandler(DoctorProfileSearchRepository repository, DoctorProfileCdcMapper mapper) {
            super(repository, mapper::toServiceInfo, DoctorProfile::getServices, DoctorProfile::setServices);
        }

        @Override
        public String getEntityName() {

            return DISPLAY_SERVICE;
        }

        @Override
        protected Long getRelationshipId(DoctorProfile.ServiceInfo entity) {
            return entity.getRelationshipId();
        }
    }

    private static class SpecialtyNestedEntityHandler extends NestedEntityHandler<DoctorProfile.SpecialtyInfo> {
        public SpecialtyNestedEntityHandler(DoctorProfileSearchRepository repository, DoctorProfileCdcMapper mapper) {
            super(repository, mapper::toSpecialtyInfo, DoctorProfile::getSpecialties, DoctorProfile::setSpecialties);
        }

        @Override
        public String getEntityName() {
            return DISPLAY_SPECIALTY;
        }

        @Override
        protected Long getRelationshipId(DoctorProfile.SpecialtyInfo entity) {
            return entity.getRelationshipId();
        }
    }


    private static class ExperienceNestedEntityHandler extends NestedEntityHandler<DoctorProfile.ExperienceInfo> {
        public ExperienceNestedEntityHandler(DoctorProfileSearchRepository repository, DoctorProfileCdcMapper mapper) {
            super(repository, mapper::toExperienceInfo, DoctorProfile::getExperiences, DoctorProfile::setExperiences);
        }

        @Override
        public String getEntityName() {
            return DISPLAY_EXPERIENCE;
        }

        @Override
        protected Long getRelationshipId(DoctorProfile.ExperienceInfo entity) {
            return entity.getRelationshipId();
        }
    }


    public PageResponse<DoctorProfileResponse> findAllDoctorProfile(int page, int size ){
        Pageable pageable = PageRequest.of(page - 1 , size);

        Page<DoctorProfile> doctorProfiles = doctorProfileRepository.findAll(pageable);

        List<DoctorProfileResponse> doctorProfileResponses =
                doctorProfiles.getContent().stream().map(doctorProfileSearchMapper::toDoctorProfileResponse).toList();

        return  PageResponse.<DoctorProfileResponse>builder()
                .currentPage(page)
                .pageSize(doctorProfiles.getSize())
                .totalPages(doctorProfiles.getTotalPages())
                .totalElements(doctorProfiles.getTotalElements())
                .data(doctorProfileResponses)
                .build();

    }

    public PageResponse<DoctorProfileResponse> searchDoctorProfiles(SearchFilter filter) {
        log.debug("Searching doctor profiles with filter: {}", filter);
        return elasticSearchProxy.searchDoctorByTerm(filter);
    }

    public List<SearchSuggestion> getDoctorSuggestions(String term, int limit) {
        log.debug("Getting doctor suggestions for term: {}, limit: {}", term, limit);
        return elasticSearchProxy.getSuggestions(term, limit);
    }

}
