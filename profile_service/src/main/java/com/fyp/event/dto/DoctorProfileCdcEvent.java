package com.fyp.event.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorProfileCdcEvent {

    @JsonProperty("payload")
    DoctorProfileCdcPayload payload;
}
