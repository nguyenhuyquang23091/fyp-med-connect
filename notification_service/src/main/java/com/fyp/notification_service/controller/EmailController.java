package com.fyp.notification_service.controller;


import com.fyp.notification_service.dto.request.ApiResponse;
import com.fyp.notification_service.dto.request.SendEmailRequest;
import com.fyp.notification_service.dto.response.EmailResponse;
import com.fyp.notification_service.service.EmailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailController {
    EmailService emailService;

    @PostMapping("/email/send")
    ApiResponse<EmailResponse> sendEmail(@RequestBody  SendEmailRequest request){
        return ApiResponse.<EmailResponse>builder().
                result(emailService.sendMail(request))
        .build();
    }

}
