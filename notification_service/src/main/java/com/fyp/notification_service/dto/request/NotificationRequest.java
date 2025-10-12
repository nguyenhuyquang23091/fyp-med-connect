package com.fyp.notification_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class NotificationRequest {
    //gui cho
    @NotNull(message = "RECIPIENT_ID_NULL")
    @NotBlank(message = "RECIPIENT_ID_EMPTY")
    protected String recipientId;

    @NotNull(message = "NOTIFICATION_TYPE_NULL")
    protected String notificationType;

    protected String title;

    protected String message;

}