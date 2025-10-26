package com.profile.vnpay.controller;


import com.corundumstudio.socketio.SocketIOServer;
import com.profile.vnpay.dto.request.PaymentRequest;
import com.profile.vnpay.dto.response.ApiResponse;
import com.profile.vnpay.dto.response.PaymentResponse;
import com.profile.vnpay.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@FieldDefaults(level =  AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {
    PaymentService paymentService;
    SocketIOServer socketIOServer;


    @PostMapping("/vn-pay")
    public ApiResponse<PaymentResponse> pay(@RequestBody PaymentRequest paymentRequest, HttpServletRequest request) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.createVnPayment(paymentRequest, request)).build();
    }

    @GetMapping("/vnpay-return")
    public ApiResponse<PaymentResponse> handleVNPayReturn(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        for (Enumeration<String> en = request.getParameterNames(); en.hasMoreElements();) {
            String key = en.nextElement();
            String value = request.getParameter(key);
            if (value != null && !value.isEmpty()) {
                params.put(key, value);
            }
        }
        // After our service handle VnPAY URL. It will emit an event to web socket where FE will register
        //to the same socket and listen to payment changes from BE side

        PaymentResponse paymentResponse = paymentService.handleVnPayReturnUrl(params);
        socketIOServer.getBroadcastOperations().sendEvent("payment:update", paymentResponse);

        return ApiResponse.<PaymentResponse>builder().result(paymentResponse).build();
    }
}