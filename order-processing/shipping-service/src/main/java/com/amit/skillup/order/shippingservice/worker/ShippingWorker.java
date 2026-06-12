package com.amit.skillup.order.shippingservice.worker;

import com.amit.skillup.order.common.enums.OrderStatus;
import com.amit.skillup.order.common.event.OrderEvent;
import com.amit.skillup.order.shippingservice.kafka.ShippingEventProducer;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShippingWorker {

    private final ShippingEventProducer eventProducer;

    /**
     * Initiates shipment. POC: always succeeds.
     * Publishes shipping-initiated Kafka event → notification-service listens.
     * Outputs: trackingId
     */
    @JobWorker(type = "ship-order")
    public void shipOrder(JobClient client, ActivatedJob job) {
        var vars        = job.getVariablesAsMap();
        String orderId    = (String) vars.get("orderId");
        String customerId = (String) vars.get("customerId");
        String productId  = (String) vars.get("productId");

        // POC: generate fake tracking ID
        String trackingId = "TRK-" + orderId.substring(0, 8).toUpperCase();
        log.info("Shipping order {}, trackingId={}", orderId, trackingId);

        // Publish Kafka event for notification-service
        eventProducer.publishShippingInitiated(OrderEvent.builder()
            .orderId(orderId)
            .customerId(customerId)
            .productId(productId)
            .status(OrderStatus.SHIPPING)
            .build());

        client.newCompleteCommand(job.getKey())
            .variables(Map.of("trackingId", trackingId))
            .send()
            .join();
    }
}
