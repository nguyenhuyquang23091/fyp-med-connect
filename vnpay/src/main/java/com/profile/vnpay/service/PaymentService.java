package com.profile.vnpay.service;


import com.profile.vnpay.constant.PaymentStatus;
import com.profile.vnpay.constant.PaymentServiceType;
import com.profile.vnpay.dto.request.PaymentRequest;
import com.profile.vnpay.dto.response.UserProfileResponse;
import com.profile.vnpay.dto.response.PaymentResponse;
import com.profile.vnpay.entity.Payment;
import com.profile.vnpay.model.PaymentInfo;
import com.profile.vnpay.repository.PaymentRepository;
import com.profile.vnpay.repository.httpClient.ProfileClient;
import com.profile.vnpay.util.VnPayUtil;
import event.dto.PaymentEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PaymentService {
    VnPayUtil vnPayUtil;
    ProfileClient profileClient;
    PaymentRepository paymentRepository;
    KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentResponse createVnPayment(PaymentRequest paymentRequest, HttpServletRequest httpServletRequest){
        //user Info

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        UserProfileResponse userProfileResponse = profileClient.getMyProfile().getResult();
        String userFullName = userProfileResponse.getFirstName() + " " + userProfileResponse.getLastName();
        String userEmail = userProfileResponse.getEmail();

        //payment info
        BigDecimal amount = paymentRequest.getAmount().multiply(BigDecimal.valueOf(100));
        log.info("Current amount is {}", amount);
        String clientIpAddress = VnPayUtil.getIpAddress(httpServletRequest);
        String txnRef = PaymentServiceType.APPOINTMENT + "_" + userId + "_" + VnPayUtil.getRandomNumber(8);

        Payment payment = Payment.builder()
                .userid(userId)
                .userFullName(userFullName)
                .userEmail(userEmail)
                .amount(paymentRequest.getAmount())  //
                .vnpayTxnRef(txnRef) // refer to VnPay TransactionId
                .referenceId(paymentRequest.getReferenceId()) //refer to productId. Example : appointmentId, courseId. ect
                .paymentStatus(PaymentStatus.PENDING)
                .paymentMethod("VnPay")
                .createdAt(Instant.now())
                .build();

        PaymentInfo paymentInfo = new PaymentInfo()
                .setReference(txnRef)
                .setAmount(amount)
                .setDescription("Payment for " + userProfileResponse.getFirstName())
                .setExpiresIn(Duration.ofMinutes(15))
                .setIpAddress(clientIpAddress);

        String paymentUrl = vnPayUtil.getPaymentURL(paymentInfo);

        paymentRepository.save(payment);

        return PaymentResponse.builder()
                .txnRef(txnRef)
                .amount(paymentRequest.getAmount())
                .status(PaymentStatus.PENDING.name())
                .message("Create Payment Successfully")
                .paymentUrl(paymentUrl)
                .build();
    }

    public PaymentResponse handleVnPayReturnUrl(Map<String, String> params){
        String receivedHash = params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        String calculatedHash = vnPayUtil.hashAllFields(params);
        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String bankCode = params.get("vnp_BankCode");
        String payDate = params.get("vnp_PayDate");
        boolean isValidSignature = receivedHash != null && receivedHash.equals(calculatedHash);

        boolean isSuccess = "00".equals(responseCode) && isValidSignature;

        if(isSuccess){
            updatePaymentStatus(txnRef, PaymentStatus.COMPLETED, bankCode, payDate);
        } else {
            updatePaymentStatus(txnRef, PaymentStatus.CANCELED, bankCode, null);
        }

        return PaymentResponse.builder()
                .txnRef(txnRef)
                .status(isSuccess ? PaymentStatus.COMPLETED.name() : PaymentStatus.CANCELED.name())
                .message(getErrorMessage(responseCode, isValidSignature))
                .build();
    }

    private void updatePaymentStatus(String tnxRef, PaymentStatus paymentStatus, String bankCode, String payDate){
        Payment payment = paymentRepository.findByVnpayTxnRef(tnxRef).orElseThrow(() -> new RuntimeException("Payment Notfound"));
        payment.setPaymentStatus(paymentStatus);
        payment.setBankCode(bankCode);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

        if(paymentStatus == PaymentStatus.COMPLETED && payDate != null){
            payment.setPaymentDate(Instant.from(formatter.parse(payDate)));
        }
        paymentRepository.save(payment);

        if (paymentStatus == PaymentStatus.COMPLETED) {
            PaymentEvent event = PaymentEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .paymentId(payment.getId())
                    .referenceId(payment.getReferenceId())
                    .userId(payment.getUserid())
                    .amount(payment.getAmount())
                    .paymentStatus(PaymentStatus.COMPLETED.name())
                    .paymentDate(payment.getPaymentDate())
                    .vnpayTxnRef(payment.getVnpayTxnRef())
                    .build();

            kafkaTemplate.send("payment-completed-events", event);
            log.info("Payment completed event sent for appointment: {}", payment.getReferenceId());
        }
    }



    private String getErrorMessage(String code, boolean isValidSignature) {
        if (!isValidSignature)
            return "Signature is not valid.";
        return switch (code) {
            case "00" -> "Payment is successful";
            case "07" -> "Suspected fraudulent transaction";
            case "09" -> "Internet Banking transaction failed";
            case "10" -> "Verification failed";
            case "11" -> "Time out transaction";
            case "12" -> "Account is locked";
            case "24" -> "User cancel transaction";
            case "51" -> "Balance is not enough";
            case "65" -> "Over transaction limit";
            default -> "Transaction fail with code: " + code;
        };
    }
}
