package com.fyp.event.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorProfileCdcPayload {

    @JsonProperty("before")
    Map<String, Object> before;

    @JsonProperty("after")
    Map<String, Object> after;

    @JsonProperty("op")
    String operation;

    @JsonProperty("entity_type")
    String entityType;

    @JsonProperty("doctor_profile_id")
    String doctorProfileId;

    @JsonProperty("user_id")
    String userId;

    @JsonProperty("ts_ms")
    Long timestamp;

    @JsonProperty("source")
    Map<String, Object> source;
}