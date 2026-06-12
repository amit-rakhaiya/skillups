package com.amit.skillup.order.orderservice.worker;

import com.amit.skillup.order.common.enums.OrderStatus;
import com.amit.skillup.order.orderservice.service.OrderService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateOrderWorker {

    private final OrderService orderService;

    /**
     * Validates the incoming order (quantity > 0, amount > 0, required fields present).
     * Sets process variable "orderValid" = true/false → used by XOR gateway in BPMN.
     */
    @JobWorker(type = "validate-order")
    public void validateOrder(JobClient client, ActivatedJob job) {
        String orderId  = (String) job.getVariablesAsMap().get("orderId");
        int    quantity = ((Number) job.getVariablesAsMap().get("quantity")).intValue();
        double amount   = ((Number) job.getVariablesAsMap().get("amount")).doubleValue();

        log.info("Validating order {}", orderId);

        boolean valid = orderId != null && !orderId.isBlank()
            && quantity > 0
            && amount > 0;

        if (valid) {
            orderService.updateStatus(orderId, OrderStatus.PAYMENT_PROCESSING);
            log.info("Order {} validated successfully", orderId);
        } else {
            orderService.updateStatus(orderId, OrderStatus.FAILED);
            log.warn("Order {} validation failed", orderId);
        }

        client.newCompleteCommand(job.getKey())
            .variables(Map.of("orderValid", valid))
            .send()
            .join();
    }
}
