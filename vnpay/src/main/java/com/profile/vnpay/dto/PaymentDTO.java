package com.profile.vnpay.dto;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

public abstract class PaymentDTO {
    @Builder
    @AllArgsConstructor(access = AccessLevel.PUBLIC)
    public static class VNPayResponse {
        public String code;
        public String message;
        public String paymentUrl;
    }
}