package com.fyp.notification_service.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentNotificationRequest extends NotificationRequest {

    //field in appointment entity
    String appointmentId;
    String patientFullName;
    String reasons;
    String phoneNumber;
    String appointmentDateTime; // ISO-8601 format string for cross-service compatibility


}
