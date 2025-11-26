package com.fyp.video_call_service.service;


import com.corundumstudio.socketio.SocketIOClient;
import com.fyp.video_call_service.constant.SessionStatus;
import com.fyp.video_call_service.dto.request.ICECandidateMessage;
import com.fyp.video_call_service.dto.request.RoomRequest;
import com.fyp.video_call_service.dto.request.WebRTCSignal;
import com.fyp.video_call_service.entity.SessionVideoCall;
import com.fyp.video_call_service.exceptions.AppException;
import com.fyp.video_call_service.exceptions.ErrorCode;
import com.fyp.video_call_service.repository.SessionVideoCallRepository;
import com.fyp.video_call_service.repository.httpCLient.AppointmentFeignClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.Socket;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VideoCallService {

    RoomStateService roomStateService;
    SessionVideoCallRepository sessionVideoCallRepository;
    AppointmentFeignClient appointmentFeignClient;


    public void handleJoinRoom(SocketIOClient client, RoomRequest roomRequest){
        String clientSessionId = client.getSessionId().toString();
        String room = roomRequest.getRoom();

        // Get room client count from the client's namespace
        int connectedClient = client.getNamespace().getRoomOperations(room).getClients().size();
        if (connectedClient == 0) {
            client.joinRoom(room);
            client.sendEvent("created", room);

            roomStateService.addUserToRoom(clientSessionId, room);

            roomStateService.markRoomCreator(room, clientSessionId);

        } else if (connectedClient == 1) {

            SessionVideoCall session = sessionVideoCallRepository.findByRoomId(room)
                    .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_EXISTED));

            if (session.getActualStartTime() == null) {
                session.setActualStartTime(LocalDateTime.now());
                sessionVideoCallRepository.save(session);
            }

                client.joinRoom(room);
            client.sendEvent("joined", room);
            roomStateService.addUserToRoom(clientSessionId, room);

            String caller = roomStateService.getRoomCreator(room);
            client.sendEvent("setCaller", caller);



        } else {
            client.sendEvent("full", room);
            log.warn("Room full - roomId: {}, rejected user: {}", room, clientSessionId);
        }

    }

    public void handleOffer(SocketIOClient client, WebRTCSignal signal){
        String roomId = signal.getRoom();
        Object sdp = signal.getSdp();

        log.info("Offer received - sessionId: {}, roomId: {}", client.getSessionId(), roomId);
        broadcastToOther(client, roomId, "offer", sdp);
    }


    public void handleReady(SocketIOClient client, RoomRequest roomRequest){
        String roomId = roomRequest.getRoom();

        log.info("Ready event - sessionId: {}, roomId: {}", client.getSessionId(), roomId);
        broadcastToOther(client, roomId, "ready", null);
    }

    public void handleCandidate(SocketIOClient client, ICECandidateMessage candidateMessage){
        String roomId = candidateMessage.getRoom();
        Object candidate = candidateMessage.getCandidate();

        log.debug("ICE candidate received - sessionId: {}, roomId: {}", client.getSessionId(), roomId);
        broadcastToOther(client, roomId, "candidate", candidate);
    }

    public void handleAnswer(SocketIOClient client, WebRTCSignal signal){
        String roomId = signal.getRoom();
        Object sdp = signal.getSdp();

        log.info("Answer received - sessionId: {}, roomId: {}", client.getSessionId(), roomId);
        broadcastToOther(client, roomId, "answer", sdp);
    }


    public void handleLeaveRoom(SocketIOClient client, RoomRequest roomRequest){
        String clientSessionId = client.getSessionId().toString();
        String room = roomRequest.getRoom();

        log.info("Leave room request - sessionId: {}, roomId: {}", clientSessionId, room);

        client.leaveRoom(room);
        roomStateService.removeUserFromRoom(clientSessionId);

        client.getNamespace().getRoomOperations(room)
                .sendEvent("userLeft", clientSessionId);
    }

    public void handleEndCall(SocketIOClient socketIOClient, RoomRequest roomRequest){
        String roomId = roomRequest.getRoom();
        String sessionId = socketIOClient.getSessionId().toString();

        log.info("=== END CALL EVENT RECEIVED ===");
        log.info("SessionId: {}", sessionId);
        log.info("RoomId: {}", roomId);

        SessionVideoCall sessionVideoCall =
                sessionVideoCallRepository.findByRoomId(roomId).orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_EXISTED));

        log.info("Found session in DB - Current status: {}, Start time: {}",
                sessionVideoCall.getSessionStatus(),
                sessionVideoCall.getActualStartTime());

        sessionVideoCall.setSessionStatus(SessionStatus.COMPLETED);
        sessionVideoCall.setActualEndTime(LocalDateTime.now());

        if(sessionVideoCall.getActualStartTime() != null ){
            long minute = ChronoUnit.MINUTES.between(sessionVideoCall.getActualStartTime(),
                    sessionVideoCall.getActualEndTime());

            sessionVideoCall.setDuration((int) minute);
            log.info("Calculated duration: {} minutes", minute);
        } else {
            log.warn("No start time recorded for session - duration will be null");
        }

        SessionVideoCall savedSession = sessionVideoCallRepository.save(sessionVideoCall);
        log.info("Session saved successfully - New status: {}, End time: {}, Duration: {}",
                savedSession.getSessionStatus(),
                savedSession.getActualEndTime(),
                savedSession.getDuration());

        appointmentFeignClient.markAppointmentComplete(savedSession.getAppointmentId());
        log.info("Appointment {} marked as COMPLETED", savedSession.getAppointmentId());

        broadcastToOther(socketIOClient, roomId, "callEnded", null);
        log.info("Broadcasted 'callEnded' event to other participants in room {}", roomId);

        roomStateService.cleanupRoom(roomId);
        log.info("Room cleanup completed for roomId: {}", roomId);
        log.info("=== END CALL HANDLING COMPLETED ===");

    }




    private void broadcastToOther(SocketIOClient sender,
                                  String roomId,
                                  String eventName, Object data){
        sender.getNamespace().getRoomOperations(roomId)
                .getClients()
                .stream()
                .filter(client -> !client.getSessionId().equals(sender.getSessionId()))
                .forEach(client -> client.sendEvent(eventName, data));

        log.debug("Broadcasted '{}' event to room {} (excluding sender)", eventName, roomId);
    }




}
