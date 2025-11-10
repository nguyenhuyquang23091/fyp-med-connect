package com.fyp.video_call_service.listener;


import com.fyp.video_call_service.constant.SessionStatus;
import com.fyp.video_call_service.entity.SessionVideoCall;
import com.fyp.video_call_service.repository.httpCLient.SessionVideoCallRepository;
import event.dto.SessionVideoEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VideoCallCreatedListener {

    SessionVideoCallRepository sessionVideoCallRepository;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 30000),
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "true"
    )
    @KafkaListener(topics = "video-call-events", groupId = "video-call-group")
    public void handleVideoCallSessionCreated(SessionVideoEvent event){
        log.info("Received Kafka event for appointment: {}", event.getAppointmentId());
        log.info("Event details - RoomId: {}, DoctorId: {}, PatientId: {}, ScheduledTime: {}",
                event.getRoomId(), event.getDoctorId(), event.getPatientId(), event.getScheduledTime());

        // Parse scheduledTime from String to LocalDateTime
        // Reason: This may throw DateTimeParseException if format is invalid, triggering retry
        LocalDateTime scheduledTime = LocalDateTime.parse(event.getScheduledTime());

        //create 5 min before start
        LocalDateTime createRoomAt = scheduledTime.minusMinutes(5);

        SessionVideoCall sessionVideoCall =
                SessionVideoCall
                        .builder()
                        .appointmentId(event.getAppointmentId())
                        .roomId(event.getRoomId())
                        .doctorId(event.getDoctorId())
                        .patientId(event.getPatientId())
                        .sessionStatus(SessionStatus.SCHEDULED)
                        .scheduledStartTime(createRoomAt)
                        .actualStartTime(scheduledTime)
                        .createdAt(LocalDateTime.now())
                        .build();

        // Reason: This may throw DataAccessException if DB is down, triggering retry
        sessionVideoCallRepository.save(sessionVideoCall);

        log.info("Successfully created video call session for appointment: {}", event.getAppointmentId());
    }

    @DltHandler
    public void handleDltVideoCallEvent(@Payload SessionVideoEvent event,
                                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                        @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {
        log.error("  Original Topic: {}", topic);
        log.error("  Appointment ID: {}", event.getAppointmentId());
        log.error("  Room ID: {}", event.getRoomId());
        log.error("  Doctor ID: {}", event.getDoctorId());
        log.error("  Patient ID: {}", event.getPatientId());
        log.error("  Scheduled Time: {}", event.getScheduledTime());
        log.error("  Error Message: {}", exceptionMessage);

        // - Save to error/audit table for manual review
        // - Send notification to admin/monitoring system
        // - Trigger alert (email, Slack, PagerDuty, etc.)
    }
}
