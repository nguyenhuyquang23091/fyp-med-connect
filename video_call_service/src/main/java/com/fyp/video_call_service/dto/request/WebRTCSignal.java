package com.fyp.video_call_service.dto.request;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class WebRTCSignal {

    String room;
    Object sdp; //including data like ip addresses, ports,


}
