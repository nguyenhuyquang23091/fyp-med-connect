package com.fyp.search_service.listener;


import ch.qos.logback.core.util.Duration;
import com.fyp.event.dto.DebeziumPayload;
import com.fyp.event.dto.DoctorProfileCdcEvent;
import com.fyp.event.dto.DoctorProfileCDCPayload;
import com.fyp.search_service.service.AppointmentSearchService;
import com.fyp.search_service.service.DoctorProfileSearchService;
import com.fyp.search_service.service.UserSearchService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.RetriableException;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
@Component
public class KafkaCdcEventListener {

    UserSearchService userSearchService;
    AppointmentSearchService appointmentSearchService;
    DoctorProfileSearchService doctorProfileSearchService;


    //4 attempts mean
    //1 retry in main topic +  3 retry topics
    @RetryableTopic(attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 30000)
            ,dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "true"
    )
    @KafkaListener(topics = "cdc.authservice.public.users", groupId = "search-service-cdc-group")
    public void handleUserEventListener(DebeziumPayload payload)  {
        String operation = payload.getOperation();

        log.debug("Processing user CDC event - operation: {}, timestamp: {}", operation, payload.getTimestamp());

        // Reason: Throw exceptions to trigger Kafka retry mechanism for transient failures
        switch (operation) {
            case "c", "r", "u":
                if (payload.getAfter() != null) {
                    userSearchService.saveUser(payload.getAfter());
                } else {
                    log.warn("Received {} operation but 'after' is null", operation);
                }
                break;
            case "d":
                if (payload.getBefore() != null) {
                    String userId = payload.getBefore().get("id").toString();
                    userSearchService.deleteUser(userId);
                } else {
                    log.warn("Received delete operation but 'before' is null");
                }
                break;
            default:
                log.warn("Unknown operation type {} for user CDC event", operation);
        }
    }

    @RetryableTopic(attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 30000)
            ,dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "true"
    )
    @KafkaListener(topics = "cdc.appointmentservice.public.appointments", groupId = "search-service-cdc-group")
    public void handleAppointmentEventListener(DebeziumPayload payload) {
        String operation = payload.getOperation();

        log.debug("Processing appointment CDC event - operation: {}, timestamp: {}", operation, payload.getTimestamp());

        // Reason: Throw exceptions to trigger Kafka retry mechanism for transient failures
        switch (operation) {
            case "c", "r", "u":
                if (payload.getAfter() != null) {
                    appointmentSearchService.saveAppointment(payload.getAfter());
                } else {
                    log.warn("Received {} operation but 'after' is null", operation);
                }
                break;

            case "d":
                if (payload.getBefore() != null) {
                    String appointmentId = payload.getBefore().get("id").toString();
                    appointmentSearchService.deleteAppointment(appointmentId);
                } else {
                    log.warn("Received delete operation but 'before' is null");
                }
                break;
            default:
                log.warn("Unknown appointment operation type: {}", operation);
        }
    }

    @RetryableTopic(attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 30000)
            ,dltStrategy = DltStrategy.FAIL_ON_ERROR, //this means no_retry on DLT topic
            autoCreateTopics = "true"
    )
    @KafkaListener(topics = "cdc.profileservice.doctor_profile_relationships", groupId = "search-service-cdc-group")
    public void handleDoctorProfileEventListener(DoctorProfileCdcEvent event){


            DoctorProfileCDCPayload doctorProfileCDCPayload = event.getPayload();
            if (doctorProfileCDCPayload == null) {
                log.warn("Received doctor profile event with null payload.");
                return;
            }

            doctorProfileSearchService.handleDoctorProfileCdcEvent(
                    doctorProfileCDCPayload.getOperation(),
                    doctorProfileCDCPayload.getEntityType(),
                    doctorProfileCDCPayload.getBefore(),
                    doctorProfileCDCPayload.getAfter(),
                    doctorProfileCDCPayload.getDoctorProfileId(),
                    doctorProfileCDCPayload.getUserId()
            );

    }

    @DltHandler
    public void listenDLT(@Payload Object message , @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                          @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {
        log.error(" CRITICAL: Message failed after all retries and sent to DLQ: {}", message);
        log.error("   Original Topic: {}", topic);
        log.error("   Error: {}", exceptionMessage);
        log.error("   Failed Message: {}", message);
        //will add send back notification to corresponding services later


    }
}
