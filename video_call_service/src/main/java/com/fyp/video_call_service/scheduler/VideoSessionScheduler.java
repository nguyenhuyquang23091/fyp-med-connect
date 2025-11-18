package com.fyp.video_call_service.scheduler;


import com.fyp.video_call_service.configuration.AuthenticationInterceptor;
import com.fyp.video_call_service.constant.SessionStatus;
import com.fyp.video_call_service.entity.SessionVideoCall;
import com.fyp.video_call_service.repository.SessionVideoCallRepository;
import com.fyp.video_call_service.service.SessionVideoCallService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class VideoSessionScheduler {

    static final long _30_SECONDS_IN_MILLISECONDS = 30_000;

    final SessionVideoCallRepository sessionVideoCallRepository;

    final SessionVideoCallService sessionVideoCallService;


    @Value("${service.internal.auth-token}")
    String serviceAuthToken;



    @Scheduled(fixedDelay = _30_SECONDS_IN_MILLISECONDS)
    public void createPendingVideoRooms(){
        try {
            // Set auth token in ThreadLocal for this scheduled thread
            // Reason: Feign client needs Authorization header, but scheduled threads don't have request context
            AuthenticationInterceptor.setAuthToken(serviceAuthToken);
            log.debug("Service auth token set for scheduled video room creation in thread: {}",
                    Thread.currentThread().getName());

            LocalDateTime now = LocalDateTime.now();

            List<SessionVideoCall> sessionsToCreate = sessionVideoCallRepository.findBySessionStatusAndScheduledStartTimeBefore(
                    SessionStatus.SCHEDULED, now.plusMinutes(1)
            ).orElse(List.of());

            if(sessionsToCreate.isEmpty()){
                log.info("No video call sessions were found to activate in thread: {}",
                        Thread.currentThread().getName());
                return;
            }

            log.info("Found {} video call session(s) to activate in thread: {}",
                    sessionsToCreate.size(),
                    Thread.currentThread().getName());

            for(SessionVideoCall sessionVideoCall : sessionsToCreate){
                try {
                    sessionVideoCallService.createPendingVideoRoom(sessionVideoCall);
                    log.info("Successfully activated video room for session ID: {} in thread: {}",
                            sessionVideoCall.getId(),
                            Thread.currentThread().getName());
                } catch (Exception e) {
                    log.error("Failed to activate video room for session ID: {} in thread: {}",
                            sessionVideoCall.getId(),
                            Thread.currentThread().getName(),
                            e);
                    // Continue processing other sessions even if one fails
                }
            }

        } catch (Exception e) {
            log.error("Error in scheduled video room creation in thread: {}",
                    Thread.currentThread().getName(),
                    e);
        } finally {
            // Always clear ThreadLocal to prevent memory leaks
            // Reason: Thread pool reuses threads, so we must clean up ThreadLocal data
            AuthenticationInterceptor.clearAuthToken();
        }
    }

}