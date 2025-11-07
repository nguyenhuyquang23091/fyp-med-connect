package com.fyp.video_call_service.configuration;

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



@org.springframework.context.annotation.Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@RequiredArgsConstructor
public class SocketIOConfiguration {

    @Value("${socket.host}")
    private String host;

    @Value("${socket.port}")
    private Integer port;


    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = createSocketIOConfiguration();
        SocketIOServer server = new SocketIOServer(config);

        log.info("SocketIOServer bean created successfully");
        return server;
    }

    private Configuration createSocketIOConfiguration() {
        Configuration config = new Configuration();
        config.setHostname(host);
        config.setPort(port);

        // CORS configuration - Allow all origins for testing
        config.setOrigin("*");
        config.setAllowCustomRequests(true);
        
        // Additional CORS and connection settings
        config.setAllowHeaders("*");


    

        // Increase timeouts and buffer sizes for stability
        config.setPingTimeout(60000);  // 60 seconds
        config.setPingInterval(25000); // 25 seconds
        config.setMaxFramePayloadLength(1048576); // 1MB
        config.setMaxHttpContentLength(1048576);  // 1MB

        config.setTransports(com.corundumstudio.socketio.Transport.POLLING,
                           com.corundumstudio.socketio.Transport.WEBSOCKET);

        // HTTP and WebSocket compression
        config.setHttpCompression(true);
        config.setWebsocketCompression(true);

        // Thread configuration for better performance and stability
        config.setBossThreads(1);
        config.setWorkerThreads(100);


        // Configure SocketIO JSON serialization with JavaTimeModule for Instant/LocalDateTime support
        config.setJsonSupport(new JacksonJsonSupport() {
            @Override
            protected void init(ObjectMapper objectMapper) {

                super.init(objectMapper);

                objectMapper.registerModule(new JavaTimeModule());
                // Write dates as ISO-8601 strings instead of timestamps
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                // Don't fail on unknown properties
                objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

            }
        });

        log.info("Socket.IO Server Configuration:");
        log.info("  Host: {}", host);
        log.info("  Port: {}", port);
        log.info("  Ping Timeout: {}ms", config.getPingTimeout());
        log.info("  Ping Interval: {}ms", config.getPingInterval());
        log.info("  Transports: POLLING, WEBSOCKET");
        log.info("  HTTP Compression: {}", config.isHttpCompression());

        return config;
    }


}