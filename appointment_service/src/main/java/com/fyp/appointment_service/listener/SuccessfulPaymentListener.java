package com.fyp.appointment_service.listener;


import com.fyp.appointment_service.configuration.AuthenticationInterceptor;
import com.fyp.appointment_service.dto.request.AppointmentNotificationRequest;
import com.fyp.appointment_service.entity.AppointmentEntity;
import com.fyp.appointment_service.exceptions.AppException;
import com.fyp.appointment_service.exceptions.ErrorCode;
import com.fyp.appointment_service.repository.AppointmentRepository;
import com.fyp.appointment_service.repository.httpCLient.NotificationFeignClient;
import event.dto.PaymentEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class SuccessfulPaymentListener {
    final AppointmentRepository appointmentRepository;
    final NotificationFeignClient notificationFeignClient;

    @Value("${service.internal.auth-token}")
    String serviceAuthToken;

    @KafkaListener(topics = "payment-completed-events", groupId = "appointment-service-group")
    public void handlePaymentCompleted(PaymentEvent event) {
        log.info("Received payment completed event for appointment: {}", event.getReferenceId());

        String appointmentId = event.getReferenceId();

        AppointmentEntity appointment = appointmentRepository
                .findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        try {
            AuthenticationInterceptor.setAuthToken(serviceAuthToken);

            AppointmentNotificationRequest notification =
                    AppointmentNotificationRequest.createdAppointment(appointment, appointment.getDoctorId());
            notificationFeignClient.sendAppointmentNotification(notification);

            log.info("Notification sent to doctor {} for paid appointment {}",
                    appointment.getDoctorId(), appointmentId);
        } finally {
            AuthenticationInterceptor.clearAuthToken();
        }
    }
}
