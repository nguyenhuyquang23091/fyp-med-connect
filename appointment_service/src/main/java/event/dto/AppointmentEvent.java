package event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentEvent {
    String eventId; // For idempotency
    String appointmentId;
    String userId;
    String doctorId;
    String specialty;
    String consultationType;
    String appointmentStatus;
    BigDecimal price;
    String appointmentDateTime;
    String createdAt;
}
