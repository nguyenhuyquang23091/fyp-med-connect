package com.profile.vnpay.entity;


import com.profile.vnpay.constant.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @Column(nullable = false)
    String userFullName;
    @Column(nullable = false)
    String userEmail;
    @Column(nullable = false)
    String userid;
    String referenceId;
    String transactionId;
    String responseCode;
    String  vnpayTxnRef;
    String bankCode;
    BigDecimal amount;
    @Enumerated(EnumType.STRING)
    PaymentStatus paymentStatus;
    String paymentMethod;
    Instant createdAt;
    Instant updatedAt;
    Instant paymentDate;




}
