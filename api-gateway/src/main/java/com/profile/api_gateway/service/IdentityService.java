package com.profile.api_gateway.service;


import com.profile.api_gateway.dto.request.ApiResponse;
import com.profile.api_gateway.dto.request.IntrospectRequest;
import com.profile.api_gateway.dto.response.IntrospectResponse;
import com.profile.api_gateway.repository.IdentityRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IdentityService {
IdentityRepository identityRepository;
public Mono<ApiResponse<IntrospectResponse>> introspect(String token){

    return identityRepository.introspect(IntrospectRequest.builder()
                     .token(token)
            .build());
}
}
