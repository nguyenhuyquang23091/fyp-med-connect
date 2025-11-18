package com.fyp.video_call_service.service;


import com.fyp.video_call_service.constant.SessionStatus;
import com.fyp.video_call_service.dto.request.VideoCallNotificationRequest;
import com.fyp.video_call_service.entity.SessionVideoCall;
import com.fyp.video_call_service.repository.SessionVideoCallRepository;
import com.fyp.video_call_service.repository.httpCLient.NotificationFeignClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SessionVideoCallService {

    SessionVideoCallRepository videoCallRepository;
    NotificationFeignClient notificationFeignClient;


    public void createPendingVideoRoom(SessionVideoCall sessionVideoCall){
        sessionVideoCall.setSessionStatus(SessionStatus.ACTIVE);
        videoCallRepository.save(sessionVideoCall);

        List<String> recipients = List.of(sessionVideoCall.getPatientId(), sessionVideoCall.getDoctorId());

        recipients.forEach(recipientId -> {
           VideoCallNotificationRequest notificationRequest =  VideoCallNotificationRequest.roomReady(sessionVideoCall, recipientId);
            notificationFeignClient.sendVideoCallNotification(notificationRequest);
        });
    }
}
