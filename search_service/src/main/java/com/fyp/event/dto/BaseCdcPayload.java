package com.fyp.event.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BaseCdcPayload<T> {
    @JsonProperty("before")
    T before;

    @JsonProperty("after")
    T after;

    @JsonProperty("op")
    String operation;

    @JsonProperty("ts_ms")
    Long timestamp;

    @JsonProperty("source")
    Map<String, Object> source;
}
