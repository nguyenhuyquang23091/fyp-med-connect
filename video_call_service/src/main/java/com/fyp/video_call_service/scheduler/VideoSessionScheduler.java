package com.fyp.video_call_service.scheduler;


import com.fyp.video_call_service.constant.SessionStatus;
import com.fyp.video_call_service.entity.SessionVideoCall;
import com.fyp.video_call_service.repository.httpCLient.SessionVideoCallRepository;
import com.fyp.video_call_service.service.SessionVideoCallService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VideoSessionScheduler {

static final long _30_SECONDS_IN_MILLISECONDS = 30_000;

SessionVideoCallRepository sessionVideoCallRepository;

SessionVideoCallService sessionVideoCallService;

@Scheduled(fixedDelay = _30_SECONDS_IN_MILLISECONDS)
public void createPendingVideoRooms(){

    LocalDateTime now = LocalDateTime.now();

    List<SessionVideoCall> sessionsToCreate = sessionVideoCallRepository.findBySessionStatusAndScheduledStartTimeBefore(

            SessionStatus.SCHEDULED, now.plusMinutes(1)

    //update custome xception later
    ).orElse(List.of());

    if(sessionsToCreate.isEmpty()){
        log.info("No video call sessions were found");
        return;
    }

    for( SessionVideoCall sessionVideoCall : sessionsToCreate){
        sessionVideoCallService.createPendingVideoRoom(sessionVideoCall);
    }



}



}
