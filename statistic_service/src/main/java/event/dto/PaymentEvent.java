package event.dto;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentEvent {
    String eventId;
    String paymentId;
    String referenceId;
    String userId;
    BigDecimal amount;
    String paymentStatus;
    Instant paymentDate;
    String vnpayTxnRef;
}
