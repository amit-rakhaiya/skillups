package com.amit.skillup.order.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private String orderId;
    private String customerId;
    private String type;   // ORDER_CONFIRMED, ORDER_FAILED, SHIPPED
    private String message;

    @Builder.Default
    private Instant timestamp = Instant.now();
}
