package com.amit.skillup.order.orderservice.service;

import com.amit.skillup.order.common.enums.OrderStatus;
import com.amit.skillup.order.common.event.OrderEvent;
import com.amit.skillup.order.orderservice.entity.Order;
import com.amit.skillup.order.orderservice.kafka.OrderEventProducer;
import com.amit.skillup.order.orderservice.repository.OrderRepository;
import io.camunda.zeebe.client.ZeebeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer eventProducer;
    private final ZeebeClient zeebeClient;

    @Transactional
    public Order createOrder(Order order) {
        // 1. Save order to DB
        Order saved = orderRepository.save(order);
        log.info("Order {} saved with status PENDING", saved.getId());

        // 2. Start Camunda process
        var processInstance = zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("order_processing")
            .latestVersion()
            .variables(Map.of(
                "orderId",    saved.getId(),
                "customerId", saved.getCustomerId(),
                "productId",  saved.getProductId(),
                "quantity",   saved.getQuantity(),
                "amount",     saved.getAmount()
            ))
            .send()
            .join();

        // 3. Store Camunda process instance key
        saved.setCamundaProcessInstanceKey(
            String.valueOf(processInstance.getProcessInstanceKey()));
        saved.setStatus(OrderStatus.VALIDATING);
        saved.setUpdatedAt(Instant.now());
        orderRepository.save(saved);
        log.info("Camunda process {} started for order {}", processInstance.getProcessInstanceKey(), saved.getId());

        // 4. Publish Kafka event — notify other services
        eventProducer.publishOrderCreated(OrderEvent.builder()
            .orderId(saved.getId())
            .customerId(saved.getCustomerId())
            .productId(saved.getProductId())
            .quantity(saved.getQuantity())
            .amount(saved.getAmount())
            .status(OrderStatus.PENDING)
            .build());

        return saved;
    }

    @Transactional
    public void updateStatus(String orderId, OrderStatus status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            order.setUpdatedAt(Instant.now());
            orderRepository.save(order);
            log.info("Order {} status updated to {}", orderId, status);
        });
    }
}
