package com.fyp.profile_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fyp.profile_service.constant.PredefinedNotificationType;
import com.fyp.profile_service.entity.PrescriptionAccess;
import com.fyp.profile_service.entity.UserPrescription;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrescriptionNotification {

    // Fields from NotificationRequest base (needed by Notification Service)
    @NotNull
    @NotBlank
    String recipientId; // Match field name in Notification Service

    @NotNull
    String notificationType;

    String title;
    String message;

    // Prescription-specific fields

    String requestId;

    @NotNull
    String prescriptionId;

    String prescriptionName;
    String requestReason;

    // Business logic fields (needed by Profile Service)
    String doctorUserId;
    String patientUserId;

    // Factory methods
    public static PrescriptionNotification accessRequest(PrescriptionAccess accessRequest) {
        return PrescriptionNotification.builder()
                .recipientId(accessRequest.getPatientUserId())
                .notificationType(PredefinedNotificationType.ACCESS_REQUEST)
                .title("Prescription Access Request")
                .message(accessRequest.getRequestReason())
                .requestId(accessRequest.getId())
                .prescriptionId(accessRequest.getPrescriptionId())
                .prescriptionName(accessRequest.getPrescriptionName())
                .requestReason(accessRequest.getRequestReason())
                .doctorUserId(accessRequest.getDoctorUserId())
                .patientUserId(accessRequest.getPatientUserId())
                .build();
    }

    public static PrescriptionNotification accessApproved(PrescriptionAccess approvedAccess) {
        return PrescriptionNotification.builder()
                .recipientId(approvedAccess.getDoctorUserId())
                .notificationType(PredefinedNotificationType.ACCESS_APPROVED)
                .title("Access Request Approved")
                .message("Your prescription access request has been approved")
                .requestId(approvedAccess.getId())
                .prescriptionId(approvedAccess.getPrescriptionId())
                .prescriptionName(approvedAccess.getPrescriptionName())
                .doctorUserId(approvedAccess.getDoctorUserId())
                .patientUserId(approvedAccess.getPatientUserId())
                .build();
    }

    public static PrescriptionNotification accessDenied(PrescriptionAccess deniedAccess) {
        return PrescriptionNotification.builder()
                .recipientId(deniedAccess.getDoctorUserId())
                .notificationType(PredefinedNotificationType.ACCESS_DENIED)
                .title("Access Request Denied")
                .message("Your prescription access request has been denied")
                .requestId(deniedAccess.getId())
                .prescriptionId(deniedAccess.getPrescriptionId())
                .prescriptionName(deniedAccess.getPrescriptionName())
                .doctorUserId(deniedAccess.getDoctorUserId())
                .patientUserId(deniedAccess.getPatientUserId())
                .build();
    }

    public static PrescriptionNotification prescriptionUpdate(UserPrescription userPrescription) {
        return PrescriptionNotification.builder()
                .recipientId(userPrescription.getUserId())
                .prescriptionName(userPrescription.getPrescriptionName())
                .title("Updated Prescription")
                .message("Your prescription was updated")
                .notificationType(PredefinedNotificationType.PRESCRIPTION_UPDATED)
                .prescriptionId(userPrescription.getId())
                .build();
    }
    // delete prescription
    public static PrescriptionNotification prescriptionDelete(UserPrescription userPrescription) {
        return PrescriptionNotification.builder()
                .recipientId(userPrescription.getUserId())
                .prescriptionName(userPrescription.getPrescriptionName())
                .title("Deleted Prescription")
                .message("Your prescription was deleted")
                .notificationType(PredefinedNotificationType.PRESCRIPTION_DELETED)
                .prescriptionId(userPrescription.getId())
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class RequestReason {
        @NotNull(message = "Request reason cannot be null")
        @NotBlank(message = "Request reason cannot be blank")
        String requestReason;
    }
}
