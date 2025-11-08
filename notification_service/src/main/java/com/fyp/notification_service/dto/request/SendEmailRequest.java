package com.fyp.notification_service.dto.request;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SendEmailRequest {

 Recipient to ;
 Long templateCode;
 Map<String, Object> params;

}
