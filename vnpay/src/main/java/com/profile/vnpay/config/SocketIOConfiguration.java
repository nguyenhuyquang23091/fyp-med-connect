package com.profile.vnpay.config;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.protocol.JacksonJsonSupport;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.oauth2.jwt.JwtException;
import com.corundumstudio.socketio.AuthorizationResult;


@org.springframework.context.annotation.Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@RequiredArgsConstructor
public class SocketIOConfiguration {
    private final CustomJwtDecoder customJwtDecoder;

    @Value("${socket.host:localhost}")
    private String host;

    @Value("${socket.port:9092}")
    private Integer port;

    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = createSocketIOConfiguration();
        return createSocketIOServerWithListeners(config);
    }
    private Configuration createSocketIOConfiguration() {
        Configuration config = new Configuration();
        config.setHostname(host);
        config.setPort(port);
        config.setOrigin("*");
        config.setAllowCustomRequests(true);

        // Configure SocketIO JSON serialization with JavaTimeModule for Instant/LocalDateTime support
        config.setJsonSupport(new JacksonJsonSupport() {
            @Override
            protected void init(ObjectMapper objectMapper) {
                objectMapper.registerModule(new JavaTimeModule());
                // Write dates as ISO-8601 strings instead of timestamps
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                // Don't fail on unknown properties
                objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            }
        });
        config.setAuthorizationListener(data -> {
            String token = data.getSingleUrlParam("token");
            if (token != null && !token.isEmpty()) {
               try{
                   customJwtDecoder.decode(token);
                   return AuthorizationResult.SUCCESSFUL_AUTHORIZATION;
               } catch (JwtException e){
                   log.error("Exception while decoding Jwt Token", e.getMessage());

                   return AuthorizationResult.FAILED_AUTHORIZATION;
               }
            }
            log.warn("Client connection rejected - no token provided");
            return AuthorizationResult.FAILED_AUTHORIZATION;
        });

        return config;
    }

    private SocketIOServer createSocketIOServerWithListeners(Configuration config) {
        SocketIOServer server = new SocketIOServer(config);
        server.addConnectListener(client -> {
            String token = client.getHandshakeData().getSingleUrlParam("token");
            if (token != null) {
                String userId = customJwtDecoder.decode(token).getSubject();
                client.joinRoom("user_" + userId);
                log.info("Client connected: {} joined room: user_{}", client.getSessionId(), userId);
            } else {
                log.warn("Client connected without userId: {}", client.getSessionId());
            }
        });

        server.addDisconnectListener(client -> {
            try {
                String token = client.getHandshakeData().getSingleUrlParam("token");
                if (token != null) {
                    String userId = customJwtDecoder.decode(token).getSubject();
                    log.info("Client disconnected: {} from room: user_{}", client.getSessionId(), userId);
                } else {
                    log.info("Client disconnected: {}", client.getSessionId());
                }
            } catch (JwtException e) {
                log.info("Client disconnected: {}", client.getSessionId());
            }
        });
        return server;
    }
}