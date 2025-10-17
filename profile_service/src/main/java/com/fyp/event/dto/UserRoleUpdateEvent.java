package com.fyp.event.dto;

import java.util.Set;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRoleUpdateEvent {
    String userId;
    String email;
    Set<String> oldRoles;
    Set<String> newRoles;
}
