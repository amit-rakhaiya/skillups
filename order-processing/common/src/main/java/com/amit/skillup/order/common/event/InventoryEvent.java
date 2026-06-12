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
public class InventoryEvent {

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private String orderId;
    private String productId;
    private int quantity;
    private boolean reserved;
    private String failureReason;

    @Builder.Default
    private Instant timestamp = Instant.now();
}
