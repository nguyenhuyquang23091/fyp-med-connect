package com.fyp.notification_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {
    String recipientUserId;           // Patient/Doctor user ID
    String notificationType;          // "ACCESS_REQUEST", "ACCESS_APPROVED", "ACCESS_DENIED"
    String message;                   // Notification message
    
    // Prescription-specific data
    String requestId;                 // Access request ID

    String prescriptionId;            // Related prescription
}