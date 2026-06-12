package com.amit.skillup.order.notificationservice.consumer;

import com.amit.skillup.order.common.config.KafkaTopics;
import com.amit.skillup.order.common.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Pure event-driven consumer — no Camunda dependency.
 * Listens to multiple topics and sends (simulated) notifications.
 */
@Slf4j
@Component
public class NotificationConsumer {

    @KafkaListener(topics = KafkaTopics.ORDER_COMPLETED, groupId = "notification-service")
    public void onOrderCompleted(OrderEvent event) {
        log.info("[NOTIFICATION] Order {} COMPLETED for customer {}. Sending success email...",
            event.getOrderId(), event.getCustomerId());
        // POC: log only — in prod, call email/SMS service here
        sendNotification(event.getCustomerId(),
            "Your order " + event.getOrderId() + " has been successfully placed and is being processed!");
    }

    @KafkaListener(topics = KafkaTopics.ORDER_FAILED, groupId = "notification-service")
    public void onOrderFailed(OrderEvent event) {
        log.info("[NOTIFICATION] Order {} FAILED for customer {}. Sending failure email...",
            event.getOrderId(), event.getCustomerId());
        sendNotification(event.getCustomerId(),
            "Unfortunately, your order " + event.getOrderId() + " could not be processed. Please try again.");
    }

    @KafkaListener(topics = KafkaTopics.SHIPPING_INITIATED, groupId = "notification-service")
    public void onShippingInitiated(OrderEvent event) {
        log.info("[NOTIFICATION] Order {} SHIPPED for customer {}. Sending shipping email...",
            event.getOrderId(), event.getCustomerId());
        sendNotification(event.getCustomerId(),
            "Great news! Your order " + event.getOrderId() + " has been shipped and is on its way!");
    }

    private void sendNotification(String customerId, String message) {
        // Simulate sending — replace with actual email/SMS in production
        log.info("[EMAIL→{}] {}", customerId, message);
    }
}
