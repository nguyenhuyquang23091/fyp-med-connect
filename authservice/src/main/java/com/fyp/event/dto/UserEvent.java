package com.fyp.event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

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
