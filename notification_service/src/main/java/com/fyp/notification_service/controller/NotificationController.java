package com.fyp.notification_service.controller;


import com.fyp.event.dto.NotificationEvent;
import com.fyp.notification_service.dto.request.*;
import com.fyp.notification_service.dto.response.NotificationResponse;
import com.fyp.notification_service.dto.response.PageResponse;
import com.fyp.notification_service.service.EmailService;
import com.fyp.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
public class NotificationController {
    EmailService emailService;
    NotificationService notificationService;

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

    // REST endpoint to send direct WebSocket notifications
    @PostMapping("/send-notification")
    public ApiResponse<String> sendDirectNotification(@RequestBody @Valid  PrescriptionAccessNotification notificationRequest) {
        notificationService.sendPrescriptionAccessNotification(notificationRequest);
        return ApiResponse.<String>builder()
                .result("Notification sent successfully")
                .build();
    }

    @GetMapping("/my-notifications")
    public ApiResponse<PageResponse<NotificationResponse>> getAllNotification(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ){
        return ApiResponse.<PageResponse<NotificationResponse>>builder()
                .result(notificationService.getNotificationForCurrentUsers(page, size))
                .build();
    }

    @PutMapping("/my-notifications/{notificationId}")
    public ApiResponse<NotificationResponse> markAsRead(@PathVariable  String notificationId){
        return ApiResponse.<NotificationResponse>builder()
                .result(notificationService
                        .markAsRead(notificationId)).build();
    }


    @DeleteMapping("/{notificationId}")
    public void deleteNotification(@PathVariable String notificationId){
         notificationService.deleteNotification(notificationId);
    }


    @PutMapping("/internal/mark-processed/{recipientUserId}/{requestId}")
    public ApiResponse<NotificationResponse> markNotificationAsProcessed(
            @PathVariable String recipientUserId,
            @PathVariable String requestId
    ) {
        return ApiResponse.<NotificationResponse>builder()
                .result(notificationService.markProcessedNotification(recipientUserId, requestId))
                .build();
    }

}
