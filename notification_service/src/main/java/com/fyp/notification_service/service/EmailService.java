package com.fyp.notification_service.service;


import com.fyp.notification_service.dto.request.*;
import com.fyp.notification_service.dto.response.EmailResponse;
import com.fyp.notification_service.exceptions.AppException;
import com.fyp.notification_service.exceptions.ErrorCode;
import com.fyp.notification_service.repository.httpClient.EmailClient;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailService {
    EmailClient emailClient;
    @NonFinal
    @Value("${spring.notification.email.brevo-apiKey}")
    protected String apiKey;

    public EmailResponse sendMail(SendEmailRequest sendEmailRequest){

        Map<String, Object> params = new HashMap<>();

        params.put("username", sendEmailRequest.getTo().getName());

        EmailRequest emailRequest = EmailRequest
                .builder()
                .sender
                        (Sender.builder().name("Med Connect - Your Healthcare Service")
                                .email("nguyenhuyquang230904@gmail.com").build())
                .to(List.of(sendEmailRequest.getTo()))
                .templateId(sendEmailRequest.getTemplateCode())
                .params(params)
                .build();
        try {
            return emailClient.sendEmail(apiKey, emailRequest);

        } catch ( FeignException.FeignClientException exception){
            log.error("Detail of current issue when sending email {}", exception.getMessage());
            throw new AppException(ErrorCode.CAN_NOT_SEND_EMAIL);
        }
    }



}
