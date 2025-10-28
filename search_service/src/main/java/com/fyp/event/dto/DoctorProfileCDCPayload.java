package com.fyp.event.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class  DoctorProfileCDCPayload extends BaseCdcPayload<Map<String, Object>> {

    
    @JsonProperty("entity_type")
    String entityType;
    @JsonProperty("doctor_profile_id")
    String doctorProfileId;

    @JsonProperty("user_id")
    String userId;
}
