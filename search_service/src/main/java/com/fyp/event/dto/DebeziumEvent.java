package com.fyp.event.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DebeziumEvent<T> {
    Payload<T> payload;

    //getAfterhere is an json object

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Payload<T> {

        T before;
        T after; //it can hold an array inside here
        @JsonProperty("op")
        private String operation;
        @JsonProperty("ts_ms")
        private Long timestamp;
        Map<String, Object> source;



    }
}
