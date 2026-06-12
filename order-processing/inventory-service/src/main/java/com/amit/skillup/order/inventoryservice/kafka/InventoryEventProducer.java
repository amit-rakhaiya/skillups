package com.amit.skillup.order.inventoryservice.kafka;

import com.amit.skillup.order.common.config.KafkaTopics;
import com.amit.skillup.order.common.event.InventoryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;

    public void publishInventoryUpdated(InventoryEvent event) {
        kafkaTemplate.send(KafkaTopics.INVENTORY_UPDATED, event.getOrderId(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) log.error("Failed to publish InventoryEvent: {}", ex.getMessage());
                else log.info("InventoryEvent published for order {}", event.getOrderId());
            });
    }
}
