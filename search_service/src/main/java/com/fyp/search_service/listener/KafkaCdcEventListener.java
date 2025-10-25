package com.fyp.search_service.listener;


import com.fyp.event.dto.DebeziumEvent;
import com.fyp.search_service.entity.UserEntity;
import com.fyp.search_service.service.AppointmentSearchService;
import com.fyp.search_service.service.UserSearchService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
@Component
public class KafkaCdcEventListener {

    UserSearchService userSearchService;
    AppointmentSearchService appointmentSearchService;

    @KafkaListener(topics = "cdc.authservice.public.users", groupId = "search-service-cdc-group")
    public void handleUserEventListener(DebeziumEvent.Payload<Map<String, Object>> payload)  {
        try {
                String operation = payload.getOperation();

                log.debug("Processing user CDC event - operation: {}, timestamp: {}", operation, payload.getTimestamp());

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
                       log.info("Unknown operation type {}", operation);
                }

        } catch (Exception e) {
            log.error("Error processing user CDC event", e);
        }
    }

    @KafkaListener(topics = "cdc.appointmentservice.public.appointments", groupId = "search-service-cdc-group")
    public void handleAppointmentEventListener(DebeziumEvent.Payload<Map<String, Object>> payload) {
        try {
            String operation = payload.getOperation();

            log.debug("Processing appointment CDC event - operation: {}, timestamp: {}", operation, payload.getTimestamp());

            switch (operation) {
                case "c", "r", "u":
                    if (payload.getAfter() != null) {
                        appointmentSearchService.indexAppointment(payload.getAfter());
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
                    log.info("Unknown appointment operation {}", operation);
            }
        } catch (Exception e) {
            log.error("Error processing appointment CDC event", e);
        }
    }




}
