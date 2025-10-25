package com.profile.vnpay.repository;

import com.profile.vnpay.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByVnpayTxnRef(String vnpayTxnRef);

}
