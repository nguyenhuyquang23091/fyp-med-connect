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
