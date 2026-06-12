package com.amit.skillup.order.paymentservice.kafka;

import com.amit.skillup.order.common.config.KafkaTopics;
import com.amit.skillup.order.common.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public void publishPaymentProcessed(PaymentEvent event) {
        kafkaTemplate.send(KafkaTopics.PAYMENT_PROCESSED, event.getOrderId(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) log.error("Failed to publish PaymentEvent {}: {}", event.getEventId(), ex.getMessage());
                else log.info("PaymentEvent {} published", event.getEventId());
            });
    }
}
