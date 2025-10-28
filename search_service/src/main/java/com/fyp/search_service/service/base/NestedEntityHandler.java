package com.fyp.search_service.service.base;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fyp.search_service.entity.DoctorProfile;
import com.fyp.search_service.repository.DoctorProfileSearchRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
public abstract class NestedEntityHandler<T> {
    protected final DoctorProfileSearchRepository doctorProfileRepository;
    private final Function<Map<String, Object>, T> toEntityMapper;
    private final Function<DoctorProfile, List<T>> entityGetter;
    private final BiConsumer<DoctorProfile, List<T>> entitySetter;

    protected NestedEntityHandler(
            DoctorProfileSearchRepository doctorProfileRepository,
            Function<Map<String, Object>, T> toEntityMapper,
            Function<DoctorProfile, List<T>> entityGetter,
            BiConsumer<DoctorProfile, List<T>> entitySetter
    ) {
        this.doctorProfileRepository = doctorProfileRepository;
        this.toEntityMapper = toEntityMapper;
        this.entityGetter = entityGetter;
        this.entitySetter = entitySetter;
    }


    public void add(Map<String, Object> data, String doctorProfileId, String userId) {
        DoctorProfile doctorProfile = getOrCreateDoctorProfile(doctorProfileId, userId);
        T entity = toEntityMapper.apply(data);
        List<T> entities = entityGetter.apply(doctorProfile);

        if (entities == null) {
            entities = new ArrayList<>();
            entitySetter.accept(doctorProfile, entities);
        }
        entities.add(entity);
        doctorProfileRepository.save(doctorProfile);
        log.info("Added {} to doctor profile: {}", getEntityName(), doctorProfileId);
    }

    public void update(Map<String, Object> beforeData, Map<String, Object> afterData, String doctorProfileId) {
        updateInternal(beforeData, afterData, doctorProfileId, (collection, predicate) -> collection.removeIf(predicate));
    }


    public void delete(Map<String, Object> beforeData, String doctorProfileId) {
        updateInternal(beforeData, null, doctorProfileId, (collection, predicate) -> collection.removeIf(predicate));
    }


    private void updateInternal(Map<String, Object> beforeData, Map<String, Object> afterData, String doctorProfileId, BiConsumer<List<T>, Predicate<T>> remover) {
        Optional<DoctorProfile> profileOpt = doctorProfileRepository.findById(doctorProfileId);
        if (profileOpt.isEmpty()) {
            log.warn("Doctor profile not found for update: {}", doctorProfileId);
            return;
        }

        DoctorProfile doctorProfile = profileOpt.get();
        Long relationshipId = getLongValue(beforeData, "relationshipId");

        List<T> entities = entityGetter.apply(doctorProfile);
        if (entities == null) {
            log.warn("Entity collection not found for doctor profile: {}", doctorProfileId);
            return;
        }

        remover.accept(entities, e -> getRelationshipId(e).equals(relationshipId));

        if (afterData != null) {
            T updatedEntity = toEntityMapper.apply(afterData);
            entities.add(updatedEntity);
        }

        doctorProfileRepository.save(doctorProfile);
        log.info("Updated {} in doctor profile: {}", getEntityName(), doctorProfileId);
    }

    public abstract String getEntityName();

    protected abstract Long getRelationshipId(T entity);

    private DoctorProfile getOrCreateDoctorProfile(String doctorProfileId, String userId) {
        return doctorProfileRepository.findById(doctorProfileId)
                .orElseGet(() -> DoctorProfile.builder()
                        .doctorProfileId(doctorProfileId)
                        .userId(userId)
                        .build());
    }
    private Long getLongValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                log.warn("Could not parse Long from string: {}", value);
                return null;
            }
        }
        return null;
    }
}

