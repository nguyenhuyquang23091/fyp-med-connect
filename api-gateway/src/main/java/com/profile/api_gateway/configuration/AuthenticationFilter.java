package com.profile.api_gateway.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.profile.api_gateway.dto.request.ApiResponse;
import com.profile.api_gateway.service.IdentityService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE, makeFinal = true)
public class AuthenticationFilter implements GlobalFilter, Ordered {
    IdentityService identityService;
    ObjectMapper objectMapper;
    @NonFinal
    private String[] publicEndPoints  = {
            "/identity/auth/.*",
            "/identity/users/registration",
            "/identity/auth/refresh",
            "/chatbot/ai/generate",
            "/notification/email/send",
            "/search/allDoctors",
            "/profile/specialties/getAll"
    };

    @Value("${app.api-prefix}")
    @NonFinal
    private String prefix;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //return back the result
        if(isPublicEndPoint(exchange.getRequest()))
            return  chain.filter(exchange);

        log.info("entering global filter");
        //get token from authorization header
        List<String> authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if(CollectionUtils.isEmpty(authHeader)) {
            return unauthenticated(exchange.getResponse());
        }
        String token = authHeader.get(0).replace("Bearer ", "");
        log.info("token {}", token);

        //verify token
        // using identity service
        //on error resume is a fallback exception in error handler
        return identityService.introspect(token).flatMap(
                introspectResponseApiResponse -> {
                    log.info("Introspect response {}", introspectResponseApiResponse.getResult().isValid());
                    if(introspectResponseApiResponse.getResult().isValid())
                        return chain.filter(exchange);
                    else
                        return unauthenticated(exchange.getResponse());
                }
        ).onErrorResume(throwable -> {
            log.error("Introspect error: {}", throwable.getMessage());
            return unauthenticated(exchange.getResponse());
        });
    }
    // Changed from -1 to 1 to allow rate limiter (order 0) to run first
    @Override
    public int getOrder() {
        return -1;
    }
    //accepting a request to identify whether it's in the public endpoints list
    //Loop from the api public endpoints
    private boolean isPublicEndPoint(ServerHttpRequest request){
        return Arrays.stream(publicEndPoints).anyMatch( s-> request.getURI().getPath().matches(prefix + s));
    }


    Mono<Void> unauthenticated(ServerHttpResponse response){
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(1401)
                .message("Unauthenticated")
                .build();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        String body;
        try {
            body = objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize error response", e);
            // Fallback to simple JSON string
            body = "{\"code\":1401,\"message\":\"Unauthenticated\"}";
        }

        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        //Custom filter
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

}
