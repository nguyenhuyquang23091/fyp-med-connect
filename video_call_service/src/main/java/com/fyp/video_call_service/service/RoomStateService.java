package com.fyp.video_call_service.service;


import com.corundumstudio.socketio.SocketIOClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomStateService {


    //key:sessionId, value:roomId
    Map<String, String> userRoomMap = new ConcurrentHashMap<>();
    //key:roomId, value:userId
    Map<String, String> roomCreatorMap = new ConcurrentHashMap<>();


    //the reason why we use concurrentHashMap is that inside a concurrent hashmap, it divided by segment, and each segment works separately

    public void registerConnection(String sessionId, String userId){
            log.info("Session ID: {}", sessionId);
            log.info("Current user ID is: {}", userId);
            log.info("Total connected users: {}", userRoomMap.size());
    }

    public void handleUserDisconnect(SocketIOClient socketIOClient, String sessionID){
        String roomId = userRoomMap.get(sessionID);
        if(!Objects.isNull(roomId)){
            socketIOClient.getNamespace().getRoomOperations(roomId).sendEvent("userDisconnected", sessionID);
        }

        userRoomMap.remove(sessionID);
    }


    public void addUserToRoom(String sessionId, String roomId){
        userRoomMap.put(sessionId, roomId);
        log.debug("User {} added to room {}", sessionId, roomId);
    }

    public void markRoomCreator(String roomId, String sessionId){
        roomCreatorMap.put(roomId, sessionId);

    }

    public String getRoomCreator(String roomId){

        return roomCreatorMap.get(roomId);
    }

    public String getRoomId(String sessionId){

        return userRoomMap.get(sessionId);
    }

    public String removeUserFromRoom(String sessionId) {
        String roomId = userRoomMap.remove(sessionId);
        if (roomId != null) {
            userRoomMap.put(sessionId, null);
            log.info("User {} left room {}", sessionId, roomId);
        }
        return roomId;
    }


    public boolean roomExists(String roomId) {
        return roomCreatorMap.containsKey(roomId);
    }

    public int getConnectedUserCount(){
        return userRoomMap.size();
    }

    public int getActiveRoomsCount() {
        return roomCreatorMap.size();
    }








}
