package event.dto;


import lombok.*;
import lombok.experimental.FieldDefaults;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SessionVideoEvent {
    String appointmentId;
    String roomId;
    String doctorId;
    String patientId;
    String scheduledTime; // ISO-8601 format: "2025-11-10T14:30:00"
}
