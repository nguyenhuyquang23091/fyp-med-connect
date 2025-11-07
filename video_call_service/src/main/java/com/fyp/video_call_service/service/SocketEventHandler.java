package com.fyp.video_call_service.service;


import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.fyp.video_call_service.dto.RoomRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SocketEventHandler {

    SocketIOServer server;
    static final Map<String, String> users = new HashMap<>();
     static final Map<String, String> rooms = new HashMap<>();

    @OnConnect
    public void onConnect(SocketIOClient client) {
        String clientId = client.getSessionId().toString();
        log.info("=== NEW CLIENT CONNECTED ===");
        log.info("Session ID: {}", clientId);
        log.info("Remote Address: {}", client.getRemoteAddress());
        log.info("Transport: {}", client.getTransport());
        log.info("Total users connected: {}", users.size() + 1);
        log.info("============================");
        users.put(clientId, null);
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        String clientId = client.getSessionId().toString();
        String room = users.get(clientId);
        log.info("=== CLIENT DISCONNECTED ===");
        log.info("Session ID: {}", clientId);
        log.info("Was in room: {}", room);
        if (!Objects.isNull(room)) {
            users.remove(clientId);
            client.getNamespace().getRoomOperations(room).sendEvent("userDisconnected", clientId);
        }
        log.info("Total users remaining: {}", users.size());
        log.info("===========================");
        printLog("onDisconnect", client, room);
    }

    @OnEvent("joinRoom")
    public void onJoinRoom(SocketIOClient client, RoomRequest roomRequest) {
        String room = roomRequest.getRoom();
        int connectedClients = server.getRoomOperations(room).getClients().size();
        if (connectedClients == 0) {
            client.joinRoom(room);
            client.sendEvent("created", room);
            users.put(client.getSessionId().toString(), room);
            rooms.put(room, client.getSessionId().toString());
        } else if (connectedClients == 1) {
            client.joinRoom(room);
            client.sendEvent("joined", room);
            users.put(client.getSessionId().toString(), room);
            client.sendEvent("setCaller", rooms.get(room));
        } else {
            client.sendEvent("full", room);
        }
        printLog("onJoinRoom", client, room);
    }

    @OnEvent("ready")
    public void onReady(SocketIOClient client, RoomRequest roomRequest) {
        String room = roomRequest.getRoom();
        // Broadcast to room excluding the sender
        client.getNamespace().getRoomOperations(room).getClients().stream()
                .filter(c -> !c.getSessionId().equals(client.getSessionId()))
                .forEach(c -> c.sendEvent("ready"));
        printLog("onReady", client, room);
    }

    @OnEvent("candidate")
    public void onCandidate(SocketIOClient client, Map<String, Object> payload) {
        String room = (String) payload.get("room");
        Object candidate = payload.get("candidate");
        // Broadcast to room excluding the sender
        client.getNamespace().getRoomOperations(room).getClients().stream()
                .filter(c -> !c.getSessionId().equals(client.getSessionId()))
                .forEach(c -> c.sendEvent("candidate", candidate));
        printLog("onCandidate", client, room);
    }

    @OnEvent("offer")
    public void onOffer(SocketIOClient client, Map<String, Object> payload) {
        String room = (String) payload.get("room");
        Object sdp = payload.get("sdp");
        // Broadcast to room excluding the sender
        client.getNamespace().getRoomOperations(room).getClients().stream()
                .filter(c -> !c.getSessionId().equals(client.getSessionId()))
                .forEach(c -> c.sendEvent("offer", sdp));
        printLog("onOffer", client, room);
    }

    @OnEvent("answer")
    public void onAnswer(SocketIOClient client, Map<String, Object> payload) {
        String room = (String) payload.get("room");
        Object sdp = payload.get("sdp");
        // Broadcast to room excluding the sender
        client.getNamespace().getRoomOperations(room).getClients().stream()
                .filter(c -> !c.getSessionId().equals(client.getSessionId()))
                .forEach(c -> c.sendEvent("answer", sdp));
        printLog("onAnswer", client, room);
    }

    @OnEvent("leaveRoom")
    public void onLeaveRoom(SocketIOClient client, RoomRequest roomRequest) {
        String room = roomRequest.getRoom();
        client.leaveRoom(room);
        printLog("onLeaveRoom", client, room);
    }

    private static void printLog(String header, SocketIOClient client, String room) {
        if (room == null) return;
        int size = 0;
        try {
            size = client.getNamespace().getRoomOperations(room).getClients().size();
        } catch (Exception e) {
            log.error("error ", e);
        }
        log.info("#ConncetedClients - {} => room: {}, count: {}", header, room, size);
    }


}
