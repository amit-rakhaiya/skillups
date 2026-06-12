package com.amit.skillup.order.orderservice.entity;

import com.amit.skillup.order.common.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Order {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    private String customerId;
    private String productId;
    private int quantity;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    private String camundaProcessInstanceKey;

    @Builder.Default
    private Instant createdAt = Instant.now();

    private Instant updatedAt;
}
