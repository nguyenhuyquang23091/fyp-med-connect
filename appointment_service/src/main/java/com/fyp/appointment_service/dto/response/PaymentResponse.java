package com.fyp.appointment_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {
     String paymentUrl;
     String txnRef;
     BigDecimal amount;
     String message;
     String status;
}
