package com.fyp.video_call_service.service;


import com.fyp.video_call_service.constant.SessionStatus;
import com.fyp.video_call_service.entity.SessionVideoCall;
import com.fyp.video_call_service.repository.httpCLient.SessionVideoCallRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SessionVideoCallService {

    SessionVideoCallRepository videoCallRepository;


    public void createPendingVideoRoom(SessionVideoCall sessionVideoCall){
        sessionVideoCall.setSessionStatus(SessionStatus.ACTIVE);
        videoCallRepository.save(sessionVideoCall);
    }
}
