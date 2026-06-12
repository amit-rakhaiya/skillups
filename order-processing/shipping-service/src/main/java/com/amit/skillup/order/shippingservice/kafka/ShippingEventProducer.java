package com.amit.skillup.order.shippingservice.kafka;

import com.amit.skillup.order.common.config.KafkaTopics;
import com.amit.skillup.order.common.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShippingEventProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void publishShippingInitiated(OrderEvent event) {
        kafkaTemplate.send(KafkaTopics.SHIPPING_INITIATED, event.getOrderId(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) log.error("Failed to publish ShippingEvent: {}", ex.getMessage());
                else log.info("ShippingEvent published for order {}", event.getOrderId());
            });
    }
}
