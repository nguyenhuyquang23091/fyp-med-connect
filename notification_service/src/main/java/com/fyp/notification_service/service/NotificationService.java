package com.fyp.notification_service.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.fyp.notification_service.constant.PredefinedNotificationType;
import com.fyp.notification_service.dto.request.NotificationRequest;
import com.fyp.notification_service.dto.response.PageResponse;
import com.fyp.notification_service.dto.request.PrescriptionAccessNotification;
import com.fyp.notification_service.dto.response.NotificationResponse;
import com.fyp.notification_service.entity.Notification;
import com.fyp.notification_service.exceptions.AppException;
import com.fyp.notification_service.exceptions.ErrorCode;
import com.fyp.notification_service.mapper.NotificationMapper;
import com.fyp.notification_service.repository.NotificationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationService {
    SocketIOServer socketIOServer;
    NotificationRepository notificationRepository;
    NotificationMapper notificationMapper;

    private void sendNotification(NotificationRequest notification) {
        String recipientId = notification.getRecipientId();

        Notification notificationEntity = Notification
                .builder()
                .recipientUserId(recipientId)
                .notificationType(notification.getNotificationType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(false)
                .createdAt(Instant.now())
                .metadata(extractMetadata(notification))
                .build();

        var notificationResponse = notificationMapper.toNotificationResponse(notificationRepository.save(notificationEntity));
        String roomName = "user_" + recipientId;
        socketIOServer.getRoomOperations(roomName).sendEvent("notification", notificationResponse);
        log.info("Notification sent to room: {}", roomName);

    }



    private Map<String, Object> extractMetadata(NotificationRequest notification) {
        Map<String, Object> metadata = new HashMap<>();

        if (notification instanceof PrescriptionAccessNotification accessNotif) {
            putIfNotNull(metadata, "requestId", accessNotif.getRequestId());
            putIfNotNull(metadata, "doctorId", accessNotif.getDoctorUserId());
            putIfNotNull(metadata, "prescriptionId", accessNotif.getPrescriptionId());
            putIfNotNull(metadata, "prescriptionName", accessNotif.getPrescriptionName());
            putIfNotNull(metadata, "requestReason", accessNotif.getRequestReason());
        }
        // Future: Add other notification type metadata extraction here

        return metadata;
    }


    private void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    public void sendPrescriptionAccessNotification(PrescriptionAccessNotification notification) {
        sendNotification(notification);
    }
    //future scalability, send mail when user book appointment/payment



    public NotificationResponse markAsRead(String notificationId){
        Notification notification =
                notificationRepository.findById(notificationId).orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_EXIST));
        notification.setIsRead(true);
        notificationRepository.save(notification);

        return notificationMapper.toNotificationResponse(notification);
    }

    public PageResponse<NotificationResponse> getNotificationForCurrentUsers(int page, int size){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String recipientUserId = authentication.getName();

        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        // Fetch ALL notifications (both processed and unprocessed)
        var notification = notificationRepository.findByRecipientUserId(recipientUserId, pageable);
        return PageResponse.<NotificationResponse>builder()
                .currentPage(page)
                .pageSize(notification.getSize())
                .totalPages(notification.getTotalPages())
                .totalElements(notification.getTotalElements())
                .data(notification.getContent().stream().map(notificationMapper::toNotificationResponse).toList())
                .build();
    }

    public void deleteNotification(String notificationId)
    {
        notificationRepository.deleteById(notificationId);
    }


    public NotificationResponse markProcessedNotification(String recipientUserId, String requestId){
        Notification notificationOpt = notificationRepository
                .findByRecipientUserIdAndMetadataRequestId(recipientUserId, requestId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_EXIST));

        notificationOpt.setIsProcessed(true);

        notificationRepository.save(notificationOpt);
        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("notificationType", PredefinedNotificationType.REQUEST_STATUS_CHANGED);
        statusUpdate.put("notificationId", notificationOpt.getId());
        statusUpdate.put("requestId", requestId);
        statusUpdate.put("action", "REMOVE");
        String roomName = "user_" + recipientUserId;

        socketIOServer.getRoomOperations(roomName).sendEvent("notification_update", statusUpdate);

        return notificationMapper.toNotificationResponse(notificationOpt);
    }
}

