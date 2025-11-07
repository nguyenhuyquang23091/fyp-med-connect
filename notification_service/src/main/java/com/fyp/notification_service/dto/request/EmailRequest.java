package com.fyp.notification_service.dto.request;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailRequest {

    Sender sender;

    List<Recipient> to;

    Long templateId;

    Map<String, Object> params;
}
