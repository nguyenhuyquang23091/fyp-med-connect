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
    public void handleUserEventListener(DebeziumEvent<Map<String, Object>> debeziumEvent)  {
        try {

                String operation = debeziumEvent.getPayload().getOperation();
                DebeziumEvent.Payload<Map<String, Object>> debeziumEventPayload = debeziumEvent.getPayload();

                switch (operation) {
                    case "c", "r", "u":
                        userSearchService.saveUser(debeziumEventPayload.getAfter());
                        break;
                    case "d":
                        String userId = debeziumEventPayload.getBefore().get("id").toString();
                        userSearchService.deleteUser(userId);
                        break;
                    default:
                       log.info("Unknown operation type {}", operation);
                }

        } catch (Exception e) {
            log.error("Error processing appointment CDC event", e);
        }
    }

    @KafkaListener(topics = "cdc.appointmentservice.public.appointments", groupId = "search-service-cdc-group")
    public void handleAppointmentEventListener(DebeziumEvent<Map<String, Object>> debeziumEvent) {
        try {
            String operation  = debeziumEvent.getPayload().getOperation();
            DebeziumEvent.Payload<Map<String, Object>>  debeziumEventPayload = debeziumEvent.getPayload();

            switch (operation) {
                case "c", "r", "u":
                appointmentSearchService.indexAppointment(debeziumEventPayload.getAfter());
                break;

                case "d":
                    String appointmentId =  debeziumEventPayload.getBefore().get("id").toString();
                    appointmentSearchService.deleteAppointment(appointmentId);
                    break;
                default:
                    log.info("Unknown appointment operation {}", operation);
            }
        } catch (Exception e) {
            log.error("Error processing appointment CDC event", e);
        }
    }




}
