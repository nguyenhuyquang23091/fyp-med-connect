package com.fyp.notification_service.service;


import com.fyp.notification_service.dto.request.ApiResponse;
import com.fyp.notification_service.dto.request.EmailRequest;
import com.fyp.notification_service.dto.request.SendEmailRequest;
import com.fyp.notification_service.dto.request.Sender;
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

import java.util.List;

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

        EmailRequest emailRequest = EmailRequest
                .builder()
                .sender
                        (Sender.builder().name("Huy Quang Nguyen")
                                .email("nguyenhuyquang230904@gmail.com").build())
                .to(List.of(sendEmailRequest.getTo()))
                .subject(sendEmailRequest.getSubject())
                .htmlContent(sendEmailRequest.getHtmlContent())
                .build();
        try {
            return emailClient.sendEmail(apiKey, emailRequest);

        } catch ( FeignException.FeignClientException exception){
            throw new AppException(ErrorCode.CAN_NOT_SEND_EMAIL);
        }
    }



}
