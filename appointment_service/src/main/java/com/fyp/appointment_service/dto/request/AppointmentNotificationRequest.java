package com.fyp.appointment_service.dto.request;


import com.fyp.appointment_service.constant.PredefinedNotificationType;
import com.fyp.appointment_service.entity.AppointmentEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentNotificationRequest {

    // Fields from NotificationRequest base (needed by Notification Service)
    @NotNull
    @NotBlank
    String recipientId; // Match field name in Notification Service

    @NotNull
    String notificationType;

    String title;
    String message;

    //field in appointment entity
    String appointmentId;
    String patientFullName;
    String reasons;
    String phoneNumber;
    String appointmentDateTime; // ISO-8601 format string for cross-service compatibility



    public static  AppointmentNotificationRequest updatedAppointment(AppointmentEntity appointmentEntity, String recipientId){
        return AppointmentNotificationRequest.builder()
                .recipientId(recipientId)
                .notificationType(PredefinedNotificationType.APPOINTMENT_UPDATED)
                .title("Updated Appointment Data")
                .message("Appointment detail has just been updated, please check for latest update")
                .appointmentId(appointmentEntity.getId())
                .patientFullName(appointmentEntity.getPatientFullName())
                .reasons(appointmentEntity.getReasons())
                .phoneNumber(appointmentEntity.getPhoneNumber())
                .appointmentDateTime(appointmentEntity.getAppointmentDateTime().toString())
                .build();


    }
    public static  AppointmentNotificationRequest cancelledAppointment(AppointmentEntity appointmentEntity, String recipientId){
        return AppointmentNotificationRequest.builder()
                .recipientId(recipientId)
                .notificationType(PredefinedNotificationType.APPOINTMENT_CANCELLED)
                .title("Cancelled Appointment Data")
                .message("Your appointment have just been cancelled")
                .appointmentId(appointmentEntity.getId())
                .patientFullName(appointmentEntity.getPatientFullName())
                .reasons(appointmentEntity.getReasons())
                .phoneNumber(appointmentEntity.getPhoneNumber())
                .appointmentDateTime(appointmentEntity.getAppointmentDateTime().toString())
                .build();


    }
    public static  AppointmentNotificationRequest createdAppointment(AppointmentEntity appointmentEntity, String recipientId){
        return AppointmentNotificationRequest.builder()
                .recipientId(recipientId)
                .notificationType(PredefinedNotificationType.APPOINTMENT_CREATED)
                .title("New Appointment Data")
                .message("You have just been booked for appointment consultation, please check for detail")
                .appointmentId(appointmentEntity.getId())
                .patientFullName(appointmentEntity.getPatientFullName())
                .reasons(appointmentEntity.getReasons())
                .phoneNumber(appointmentEntity.getPhoneNumber())
                .appointmentDateTime(appointmentEntity.getAppointmentDateTime().toString())
                .build();


    }





}
