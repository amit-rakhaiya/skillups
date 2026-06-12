package com.amit.skillup.order.orderservice.controller;

import com.amit.skillup.order.orderservice.entity.Order;
import com.amit.skillup.order.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/orders
     * Entry point — creates order, starts Camunda process, publishes Kafka event.
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        log.info("Received order request from customer {}", request.customerId());

        Order order = Order.builder()
            .customerId(request.customerId())
            .productId(request.productId())
            .quantity(request.quantity())
            .amount(request.amount())
            .build();

        Order created = orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** Simple request record — Java 16+ */
    record CreateOrderRequest(
        String customerId,
        String productId,
        int quantity,
        BigDecimal amount
    ) {}
}
