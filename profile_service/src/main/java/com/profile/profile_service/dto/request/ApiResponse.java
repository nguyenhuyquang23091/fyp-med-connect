package com.profile.profile_service.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    @Builder.Default
    private int code = 1000;

    private String message;
    // Response in all type of errors.
    private T result;
}
