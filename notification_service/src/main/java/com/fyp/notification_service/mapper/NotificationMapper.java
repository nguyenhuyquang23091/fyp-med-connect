package com.fyp.notification_service.mapper;


import com.fyp.notification_service.dto.response.NotificationResponse;
import com.fyp.notification_service.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponse toNotificationResponse(Notification notification);
}
