package com.fyp.notification_service.listener;

import com.fyp.event.dto.NotificationEvent;
import com.fyp.notification_service.dto.request.Recipient;
import com.fyp.notification_service.dto.request.SendEmailRequest;
import com.fyp.notification_service.service.EmailService;
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



@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationListener {

    EmailService emailService;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 30000),
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "true"
    )
    @KafkaListener(topics = "notification-delivery")
    public void listenNotificationDelivery(NotificationEvent message){
        log.info("Message receiver : {}" , message);
        if("EMAIL".equals(message.getChannel())){
            String recipientEmail = message.getRecipientEmail();
            String recipientName;
            long templateCode = message.getTemplateCode();

            if( templateCode == 3L ){
                recipientName = message.getParam().getOrDefault("username", "New Comer").toString();
            } else if (templateCode == 4L){
                recipientName = message.getParam().getOrDefault("doctorFullName", "Doctor").toString();
            } else {
                recipientName = message.getParam().getOrDefault("recipientName", "Valued User")
                        .toString();
            }
            emailService.sendMail(SendEmailRequest.builder()
                    .templateCode(message.getTemplateCode())
                    .to(Recipient
                            .builder()
                            .name(recipientName)
                            .email(recipientEmail)
                            .build()
                    )
                    .params(message.getParam())
                    .build());
        }
        // further other services like ( SMS)
    }

    
    @DltHandler
    public void handleDltNotificationEvent(@Payload NotificationEvent message,
                                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                          @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {
        log.error("  Original Topic: {}", topic);
        log.error("  Channel: {}", message.getChannel());
        log.error("  Recipient Email: {}", message.getRecipientEmail());
        log.error("  Template Code: {}", message.getTemplateCode());
        log.error("  Error Message: {}", exceptionMessage);
        log.error("  Action Required: Manual intervention needed to deliver this notification");

        // TODO: Implement additional failure handling
        // - Save to error/audit table for manual review
        // - Send notification to admin/monitoring system
        // - Trigger alert (email, Slack, PagerDuty, etc.)
    }

}
