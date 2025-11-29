package com.fyp.statistic_service.listener;



import event.dto.AppointmentEvent;
import event.dto.PaymentEvent;
import event.dto.UserEvent;
import com.fyp.statistic_service.constant.PredefinedType;
import com.fyp.statistic_service.entity.StatisticEventLogs;
import com.fyp.statistic_service.repository.StatisticEventLogRepository;
import com.fyp.statistic_service.service.AppointmentStatisticService;
import com.fyp.statistic_service.service.PaymentStatisticService;
import com.fyp.statistic_service.service.UserStatisticService;
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

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
@Component
public class StatisticEventListener {

    StatisticEventLogRepository eventLogRepository;
    UserStatisticService userStatisticService;
    AppointmentStatisticService appointmentStatisticService;
    PaymentStatisticService paymentStatisticService;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 30000),
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "true"
    )
    @KafkaListener(topics = "user-created")
    public void listenUserCreateEvent(UserEvent userEvent){

        if (eventLogRepository.existsByEventId(userEvent.getEventId())) {
            log.info("Duplicate event skipped - eventId: {}", userEvent.getEventId());
            return;
        }

        try {
            userStatisticService.processUserCreatedEvent(userEvent);

            StatisticEventLogs statisticEventLogs = StatisticEventLogs.builder()
                    .eventId(userEvent.getEventId())
                    .eventType(PredefinedType.USER_CREATED)
                    .status(PredefinedType.SUCCESS)
                    .topic("user-created")
                    .build();

            eventLogRepository.save(statisticEventLogs);

            log.info("UserEvent processed successfully - eventId: {}", userEvent.getEventId());

        } catch (Exception e) {
            log.error("Error processing UserEvent - eventId: {}", userEvent.getEventId(), e);
            throw e;
        }
    }

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 30000),
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "true"
    )
    @KafkaListener(topics = "appointment-created")
    public void listenAppointmentCreateEvent(AppointmentEvent appointmentEvent){

        if (eventLogRepository.existsByEventId(appointmentEvent.getEventId())) {
            log.info("Duplicate event skipped - eventId: {}", appointmentEvent.getEventId());
            return;
        }

        try {
            appointmentStatisticService.processAppointmentCreatedEvent(appointmentEvent);

            StatisticEventLogs statisticEventLogs = StatisticEventLogs.builder()
                    .eventId(appointmentEvent.getEventId())
                    .eventType(PredefinedType.APPOINTMENT_CREATED)
                    .status(PredefinedType.SUCCESS)
                    .topic("appointment-created")
                    .build();

            eventLogRepository.save(statisticEventLogs);

            log.info("AppointmentEvent processed successfully - eventId: {}", appointmentEvent.getEventId());

        } catch (Exception e) {
            log.error("Error processing AppointmentEvent - eventId: {}", appointmentEvent.getEventId(), e);
            throw e;
        }
    }


    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 30000),
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "true"
    )
    @KafkaListener(topics = "payment-completed-events")
    public void listenPaymentCompleteEvent(PaymentEvent paymentEvent){

        if (eventLogRepository.existsByEventId(paymentEvent.getEventId())) {
            log.info("Duplicate event skipped - eventId: {}", paymentEvent.getEventId());
            return;
        }

        try {
            paymentStatisticService.processPaymentCompletedEvent(paymentEvent);

            StatisticEventLogs statisticEventLogs = StatisticEventLogs.builder()
                    .eventId(paymentEvent.getEventId())
                    .eventType(PredefinedType.PAYMENT_COMPLETED)
                    .status(PredefinedType.SUCCESS)
                    .topic("payment-completed-events")
                    .build();

            eventLogRepository.save(statisticEventLogs);

            log.info("PaymentEvent processed successfully - eventId: {}", paymentEvent.getEventId());

        } catch (Exception e) {
            log.error("Error processing PaymentEvent - eventId: {}", paymentEvent.getEventId(), e);
            throw e;
        }
    }

    @DltHandler
    public void handleDltVideoCallEvent(
            @Payload UserEvent userEvent,
            @Payload AppointmentEvent appointmentEvent,
            @Payload PaymentEvent paymentEvent,

            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage
    ) {

        log.error("===== Dead Letter Queue Event Received =====");
        log.error("Original Topic: {}", topic);
        log.error("Error Message: {}", exceptionMessage);

        // Log User Event if present
        if (userEvent != null) {
            log.error("[UserEvent]");
            log.error("  User ID: {}", userEvent.getUserId());
            log.error("  Email: {}", userEvent.getEmail());
            log.error("  Role: {}", userEvent.getRoles());
            log.error("  Created At: {}", userEvent.getCreatedAt());
        }

        // Log Appointment Event if present
        if (appointmentEvent != null) {
            log.error("[AppointmentEvent]");
            log.error("  Appointment ID: {}", appointmentEvent.getAppointmentId());
            log.error("  User ID: {}", appointmentEvent.getUserId());
            log.error("  Doctor ID: {}", appointmentEvent.getDoctorId());
            log.error("  Consultation Type: {}", appointmentEvent.getConsultationType());
            log.error("  Appointment Status: {}", appointmentEvent.getAppointmentStatus());
            log.error("  Scheduled At: {}", appointmentEvent.getAppointmentDateTime());
            log.error("  Price: {}", appointmentEvent.getPrice());
        }

        // Log Payment Event if present
        if (paymentEvent != null) {
            log.error("[PaymentEvent]");
            log.error("  Payment ID: {}", paymentEvent.getPaymentId());
            log.error("  Amount: {}", paymentEvent.getAmount());

        }

        log.error("===== End DLT Log Block =====");

        // - Save to audit/error table
        // - Notify admin/monitoring
        // - Trigger alert
    }











}
