package com.amit.skillup.order.paymentservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Payment {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    private String orderId;
    private String customerId;
    private BigDecimal amount;

    /** SUCCESS | FAILED | REFUNDED */
    private String status;

    private String failureReason;

    @Builder.Default
    private Instant createdAt = Instant.now();

    private Instant updatedAt;
}
