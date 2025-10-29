package com.fyp.profile_service.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fyp.event.dto.CdcOperation;
import com.fyp.event.dto.DoctorProfileCdcEvent;
import com.fyp.event.dto.DoctorProfileCdcPayload;
import com.fyp.event.dto.DoctorProfileEntityType;
import com.fyp.profile_service.config.KafkaTopicConfig;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorProfileCdcProducer {

    KafkaTemplate<String, Object> kafkaTemplate;


    public void publishCdcEvent(
            CdcOperation operation,
            DoctorProfileEntityType entityType,
            Map<String, Object> before,
            Map<String, Object> after,
            String doctorProfileId,
            String userId) {

        try {
            Map<String, Object> source = new HashMap<>();
            source.put("service", "profile_service");
            source.put("db", "neo4j");
            source.put("collection", "doctor_profile_relationships");

            DoctorProfileCdcPayload payload = DoctorProfileCdcPayload.builder()
                    .before(before)
                    .after(after)
                    .operation(operation.getCode())
                    .entityType(entityType.name())
                    .doctorProfileId(doctorProfileId)
                    .userId(userId)
                    .timestamp(System.currentTimeMillis())
                    .source(source)
                    .build();

            DoctorProfileCdcEvent event = DoctorProfileCdcEvent.builder()
                    .payload(payload)
                    .build();

            kafkaTemplate.send(KafkaTopicConfig.DOCTOR_PROFILE_CDC_TOPIC, userId, event);

            log.debug(
                    "Published CDC event - operation: {}, entityType: {}, userId: {}, doctorProfileId: {}",
                    operation,
                    entityType,
                    userId,
                    doctorProfileId);

        } catch (Exception e) {
            log.error(
                    "Failed to publish CDC event - operation: {}, entityType: {}, userId: {}",
                    operation,
                    entityType,
                    userId,
                    e);
        }
    }



    public void publishCreate(
            DoctorProfileEntityType entityType, Map<String, Object> after, String doctorProfileId, String userId) {
        publishCdcEvent(CdcOperation.CREATE, entityType, null, after, doctorProfileId, userId);
    }



    public void publishUpdate(
            DoctorProfileEntityType entityType,
            Map<String, Object> before,
            Map<String, Object> after,
            String doctorProfileId,
            String userId) {
        publishCdcEvent(CdcOperation.UPDATE, entityType, before, after, doctorProfileId, userId);
    }


    public void publishDelete(
            DoctorProfileEntityType entityType, Map<String, Object> before, String doctorProfileId, String userId) {
        publishCdcEvent(CdcOperation.DELETE, entityType, before, null, doctorProfileId, userId);
    }
}