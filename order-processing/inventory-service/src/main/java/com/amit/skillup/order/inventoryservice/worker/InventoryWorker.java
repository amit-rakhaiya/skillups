package com.amit.skillup.order.inventoryservice.worker;

import com.amit.skillup.order.common.event.InventoryEvent;
import com.amit.skillup.order.inventoryservice.service.InventoryService;
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
public class InventoryWorker {

    private final InventoryService inventoryService;

    /**
     * Reserves inventory stock.
     * Outputs: inventoryReserved=true/false
     */
    @JobWorker(type = "check-inventory")
    public void checkInventory(JobClient client, ActivatedJob job) {
        var vars       = job.getVariablesAsMap();
        String orderId   = (String) vars.get("orderId");
        String productId = (String) vars.get("productId");
        int quantity     = ((Number) vars.get("quantity")).intValue();

        log.info("Checking inventory for order {}, product {}, qty {}", orderId, productId, quantity);
        InventoryEvent result = inventoryService.reserveInventory(orderId, productId, quantity);

        client.newCompleteCommand(job.getKey())
            .variables(Map.of("inventoryReserved", result.isReserved()))
            .send()
            .join();
    }

    /**
     * Compensation handler for check-inventory.
     * Releases previously reserved stock when process compensates.
     */
    @JobWorker(type = "release-inventory")
    public void releaseInventory(JobClient client, ActivatedJob job) {
        var vars       = job.getVariablesAsMap();
        String orderId   = (String) vars.get("orderId");
        String productId = (String) vars.get("productId");
        int quantity     = ((Number) vars.get("quantity")).intValue();

        log.info("Compensation: releasing inventory for order {}", orderId);
        inventoryService.releaseInventory(orderId, productId, quantity);

        client.newCompleteCommand(job.getKey()).send().join();
    }
}
