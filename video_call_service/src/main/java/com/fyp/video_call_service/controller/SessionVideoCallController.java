package com.fyp.video_call_service.controller;


import com.fyp.video_call_service.service.SessionVideoCallService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SessionVideoCallController {

    SessionVideoCallService sessionVideoCallService;
}
