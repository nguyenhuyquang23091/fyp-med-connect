package com.fyp.notification_service.dto.request;

import com.fyp.notification_service.constant.PredefinedNotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class PrescriptionAccessNotification extends NotificationRequest{

    String requestId;

    String doctorUserId;

    String patientUserId;

    @NotNull
    String prescriptionId;

    String prescriptionName;

    String requestReason;
}
