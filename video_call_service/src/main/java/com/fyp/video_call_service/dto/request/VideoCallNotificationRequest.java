package com.fyp.video_call_service.dto.request;


import com.fyp.video_call_service.constant.PredefinedNotificationType;
import com.fyp.video_call_service.entity.SessionVideoCall;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor

public class VideoCallNotificationRequest {

    // Fields from NotificationRequest base (needed by Notification Service)
    @NotNull
    @NotBlank
    String recipientId; // Match field name in Notification Service

    @NotNull
    String notificationType;

    String title;
    String message;

    //specific field in video call service

    String roomId;
    String appointmentId;
    String doctorId;
    String patientId;
    LocalDateTime scheduledStartTime;

    public static VideoCallNotificationRequest roomReady(SessionVideoCall sessionVideoCall, String recipientId){

    return VideoCallNotificationRequest
            .builder()
            .recipientId(recipientId)
            .notificationType(PredefinedNotificationType.VIDEO_CALL_ROOM_READY)
            .title("Video Call Ready")
            .message("Your appointment video call is ready to join")
            .roomId(sessionVideoCall.getRoomId())
            .doctorId(sessionVideoCall.getDoctorId())
            .patientId(sessionVideoCall.getPatientId())
           // may be convert to string to avoid communication error between services .scheduledStartTime(sessionVideoCall.getScheduledStartTime())
            .build();

    }




}
