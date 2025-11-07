package com.fyp.event.dto;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationEvent {
    String channel;
    String recipientEmail;
    String recipientUserName;
    Long templateCode;
    Map<String, Object> param;


}
