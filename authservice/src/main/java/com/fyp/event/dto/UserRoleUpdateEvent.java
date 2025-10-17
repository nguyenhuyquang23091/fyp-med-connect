package com.fyp.event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

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
