package com.fyp.statistic_service.service;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SocketIOEventEmitter {

    SocketIOServer socketIOServer;

    public void emitUserStatisticUpdate(Object data) {
        try {
            socketIOServer.getBroadcastOperations().sendEvent("user_statistic_update", data);
            log.debug("Emitted user_statistic_update event to all clients");
        } catch (Exception e) {
            log.error("Failed to emit user_statistic_update event", e);
        }
    }

    public void emitAppointmentStatisticUpdate(Object data) {
        try {
            socketIOServer.getBroadcastOperations().sendEvent("appointment_statistic_update", data);
            log.debug("Emitted appointment_statistic_update event to all clients");
        } catch (Exception e) {
            log.error("Failed to emit appointment_statistic_update event", e);
        }
    }

    public void emitPaymentStatisticUpdate(Object data) {
        try {
            socketIOServer.getBroadcastOperations().sendEvent("payment_statistic_update", data);
            log.debug("Emitted payment_statistic_update event to all clients");
        } catch (Exception e) {
            log.error("Failed to emit payment_statistic_update event", e);
        }
    }

    public void emitDashboardUpdate(Map<String, Object> dashboardData) {
        try {
            socketIOServer.getBroadcastOperations().sendEvent("dashboard_update", dashboardData);
            log.debug("Emitted dashboard_update event to all clients");
        } catch (Exception e) {
            log.error("Failed to emit dashboard_update event", e);
        }
    }

    public void emitToUser(String userId, String eventName, Object data) {
        try {
            String roomName = "user_" + userId;
            socketIOServer.getRoomOperations(roomName).sendEvent(eventName, data);
            log.debug("Emitted {} event to user: {}", eventName, userId);
        } catch (Exception e) {
            log.error("Failed to emit {} event to user: {}", eventName, userId, e);
        }
    }

    public void emitToAdmins(String eventName, Object data) {
        try {
            socketIOServer.getRoomOperations("admin_dashboard").sendEvent(eventName, data);
            log.debug("Emitted {} event to admin dashboard", eventName);
        } catch (Exception e) {
            log.error("Failed to emit {} event to admin dashboard", eventName, e);
        }
    }
}
