package com.fyp.video_call_service.configuration;

import com.corundumstudio.socketio.AuthorizationResult;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.protocol.JacksonJsonSupport;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fyp.video_call_service.dto.request.ICECandidateMessage;
import com.fyp.video_call_service.dto.request.RoomRequest;
import com.fyp.video_call_service.dto.request.WebRTCSignal;
import com.fyp.video_call_service.service.RoomStateService;
import com.fyp.video_call_service.service.VideoCallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.oauth2.jwt.JwtException;


@org.springframework.context.annotation.Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@RequiredArgsConstructor
public class SocketIOConfiguration {

    @Value("${socket.host}")
    private String host;

    @Value("${socket.port}")
    private Integer port;

    private final CustomJwtDecoder customJwtDecoder;
    private final VideoCallService videoCallService;
    private final RoomStateService roomStateService;

    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = createSocketIOConfiguration();
        return createSocketIOServerWithListeners(config);

    }

    private Configuration createSocketIOConfiguration() {
        Configuration config = new Configuration();
        config.setHostname(host);
        config.setPort(port);
        // CORS configuration - Allow all origins for testing
        config.setOrigin("*");
        config.setAllowCustomRequests(true);

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

        // Connection listener - register user when they connect
        server.addConnectListener(client -> {
            String token = client.getHandshakeData().getSingleUrlParam("token");
            String sessionId = client.getSessionId().toString();

            if (token != null) {
                String userId = customJwtDecoder.decode(token).getSubject();

                // Register connection in RoomStateService
                roomStateService.registerConnection(sessionId, userId);

                // Join user-specific room for notifications
                client.joinRoom("user_" + userId);
                log.info("Client connected: {} joined room: user_{}", sessionId, userId);
            } else {
                log.warn("Client connected without userId: {}", sessionId);
            }
        });

        // Disconnection listener - clean up user state
        server.addDisconnectListener(client -> {
            String sessionId = client.getSessionId().toString();

            // Handle disconnection through RoomStateService
            roomStateService.handleUserDisconnect(client, sessionId);

            try {
                String token = client.getHandshakeData().getSingleUrlParam("token");
                if (token != null) {
                    String userId = customJwtDecoder.decode(token).getSubject();
                    log.info("Client disconnected: {} from room: user_{}", sessionId, userId);
                } else {
                    log.info("Client disconnected: {}", sessionId);
                }
            } catch (JwtException e) {
                log.info("Client disconnected: {}", sessionId);
            }
        });

        // Register all WebRTC event listeners
        registerWebRtcEventListeners(server);

        log.info("SocketIOServer configured with all listeners");
        return server;
    }

    private void registerWebRtcEventListeners(SocketIOServer server){

        // Event 1: Join Room - User joins a video call room
        server.addEventListener("joinRoom", RoomRequest.class, (client, data, ackRequest) -> {
            videoCallService.handleJoinRoom(client, data);
        });

        // Event 2: Ready - User is ready to start WebRTC negotiation
        server.addEventListener("ready", RoomRequest.class, (client, data, ackRequest) -> {
            videoCallService.handleReady(client, data);
        });

        // Event 3: Offer - WebRTC SDP offer from caller
        server.addEventListener("offer", WebRTCSignal.class, (client, data, ackRequest) -> {
            videoCallService.handleOffer(client, data);
        });

        // Event 4: Answer - WebRTC SDP answer from callee
        server.addEventListener("answer", WebRTCSignal.class, (client, data, ackRequest) -> {
            videoCallService.handleAnswer(client, data);
        });

        // Event 5: Candidate - ICE candidate exchange
        server.addEventListener("candidate", ICECandidateMessage.class, (client, data, ackRequest) -> {
            videoCallService.handleCandidate(client, data);
        });

        // Event 6: Leave Room - User leaves video call room
        server.addEventListener("leaveRoom", RoomRequest.class, (client, data, ackRequest) -> {
            videoCallService.handleLeaveRoom(client, data);
        });

        //7 End Call

        server.addEventListener("callEnded", RoomRequest.class, (client, data, ackRequest) -> {
            videoCallService.handleEndCall(client, data);
        });

        log.info("Registered {} WebRTC event listeners", 6);
    }






}