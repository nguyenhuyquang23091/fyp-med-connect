package com.fyp.search_service.service;

import com.fyp.search_service.entity.DoctorProfile;
import com.fyp.search_service.mapper.DoctorProfileCdcMapper;
import com.fyp.search_service.repository.DoctorProfileSearchRepository;
import com.fyp.search_service.service.base.NestedEntityHandler;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.fyp.search_service.constant.PredefinedType.*;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorProfileSearchService {

    DoctorProfileSearchRepository doctorProfileRepository;
    DoctorProfileCdcMapper doctorProfileCdcMapper;
    Map<String, NestedEntityHandler<?>> entityHandlers = new HashMap<>();

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
                log.warn("Unknown entity type: {}", entityType);
            }

        } catch (Exception e) {
            log.error("Error processing doctor profile CDC event - profileId: {}, entityType: {}, operation: {}",
                    doctorProfileId, entityType, operation, e);
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

        // Preserve existing nested entities if profile already exists
        doctorProfileRepository.findById(doctorProfileId).ifPresent(existing -> {
            profile.setServices(existing.getServices());
            profile.setSpecialties(existing.getSpecialties());
            profile.setExperiences(existing.getExperiences());
            profile.setLanguages(existing.getLanguages());
        });

        doctorProfileRepository.save(profile);
        log.info("Created/Updated doctor profile in Elasticsearch - profileId: {}, userId: {}",
                 doctorProfileId, userId);
    }


    private void updateProfile(Map<String, Object> data, String doctorProfileId) {
        doctorProfileRepository.findById(doctorProfileId).ifPresentOrElse(existing -> {
            DoctorProfile updated = doctorProfileCdcMapper.toProfile(data);

            // Preserve nested entities and languages
            updated.setServices(existing.getServices());
            updated.setSpecialties(existing.getSpecialties());
            updated.setExperiences(existing.getExperiences());
            updated.setLanguages(existing.getLanguages());

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

}
