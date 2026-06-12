package com.amit.skillup.order.orderservice.kafka;

import com.amit.skillup.order.common.config.KafkaTopics;
import com.amit.skillup.order.common.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void publish(String topic, OrderEvent event) {
        // Use orderId as key → ensures ordering for same order
        kafkaTemplate.send(topic, event.getOrderId(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event {} to topic {}: {}", event.getEventId(), topic, ex.getMessage());
                } else {
                    log.info("Published event {} to topic {} partition {}",
                        event.getEventId(), topic, result.getRecordMetadata().partition());
                }
            });
    }

    public void publishOrderCreated(OrderEvent event) {
        publish(KafkaTopics.ORDER_CREATED, event);
    }

    public void publishOrderCompleted(OrderEvent event) {
        publish(KafkaTopics.ORDER_COMPLETED, event);
    }

    public void publishOrderFailed(OrderEvent event) {
        publish(KafkaTopics.ORDER_FAILED, event);
    }
}
