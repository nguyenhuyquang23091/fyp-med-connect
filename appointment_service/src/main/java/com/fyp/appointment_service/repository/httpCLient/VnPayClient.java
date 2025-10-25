package com.fyp.appointment_service.repository.httpCLient;


import com.fyp.appointment_service.configuration.AuthenticationInterceptor;
import com.fyp.appointment_service.dto.request.ApiResponse;
import com.fyp.appointment_service.dto.request.PaymentRequest;
import com.fyp.appointment_service.dto.response.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "payment-service",
        url = "${microservices.payment-service.url}",
        configuration = {AuthenticationInterceptor.class})
public interface VnPayClient {
    @PostMapping(value = "/payment/vn-pay")
    ApiResponse<PaymentResponse> createVnPayment(PaymentRequest request);
}

