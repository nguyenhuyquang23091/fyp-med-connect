package event.dto;

import java.util.Set;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class UserEvent {

    String eventId; // For idempotency
    String userId;
    String email;
    String username;
    Set<String> roles;
    String createdAt;

}
