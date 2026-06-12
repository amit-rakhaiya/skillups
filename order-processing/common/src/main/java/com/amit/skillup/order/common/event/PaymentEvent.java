package com.amit.skillup.order.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private String orderId;
    private String paymentId;
    private BigDecimal amount;
    private boolean success;
    private String failureReason;

    @Builder.Default
    private Instant timestamp = Instant.now();
}
