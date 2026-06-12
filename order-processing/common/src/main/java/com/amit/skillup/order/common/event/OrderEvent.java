package com.amit.skillup.order.common.event;

import com.amit.skillup.order.common.enums.OrderStatus;
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
public class OrderEvent {

    @Builder.Default
    private String eventId = UUID.randomUUID().toString(); // for idempotency

    private String orderId;
    private String customerId;
    private String productId;
    private int quantity;
    private BigDecimal amount;
    private OrderStatus status;

    @Builder.Default
    private Instant timestamp = Instant.now();
}
