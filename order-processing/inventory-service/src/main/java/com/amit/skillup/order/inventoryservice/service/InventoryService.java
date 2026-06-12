package com.amit.skillup.order.inventoryservice.service;

import com.amit.skillup.order.common.event.InventoryEvent;
import com.amit.skillup.order.inventoryservice.entity.InventoryItem;
import com.amit.skillup.order.inventoryservice.kafka.InventoryEventProducer;
import com.amit.skillup.order.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryEventProducer eventProducer;

    /**
     * Reserve stock for an order.
     * POC: if product not found or qty insufficient → reserved=false.
     */
    @Transactional
    public InventoryEvent reserveInventory(String orderId, String productId, int quantity) {
        InventoryItem item = inventoryRepository.findById(productId).orElse(null);

        boolean reserved;
        String failureReason = null;

        if (item == null) {
            reserved = false;
            failureReason = "Product " + productId + " not found";
        } else if (item.getAvailableQty() < quantity) {
            reserved = false;
            failureReason = "Insufficient stock: available=" + item.getAvailableQty();
        } else {
            item.setAvailableQty(item.getAvailableQty() - quantity);
            item.setReservedQty(item.getReservedQty() + quantity);
            inventoryRepository.save(item);
            reserved = true;
        }

        log.info("Inventory reserve for order {}, product {}, qty {}: {}", orderId, productId, quantity, reserved);

        InventoryEvent event = InventoryEvent.builder()
            .orderId(orderId)
            .productId(productId)
            .quantity(quantity)
            .reserved(reserved)
            .failureReason(failureReason)
            .build();

        eventProducer.publishInventoryUpdated(event);
        return event;
    }

    /**
     * Compensation: release reserved stock.
     */
    @Transactional
    public void releaseInventory(String orderId, String productId, int quantity) {
        inventoryRepository.findById(productId).ifPresent(item -> {
            item.setAvailableQty(item.getAvailableQty() + quantity);
            item.setReservedQty(Math.max(0, item.getReservedQty() - quantity));
            inventoryRepository.save(item);
            log.info("Compensation: released {} units of {} for order {}", quantity, productId, orderId);
        });
    }

    /** Seed test data on startup */
    @Transactional
    public void seedData() {
        if (!inventoryRepository.existsById("PROD-001")) {
            inventoryRepository.save(InventoryItem.builder()
                .productId("PROD-001")
                .productName("Laptop")
                .availableQty(100)
                .reservedQty(0)
                .build());
            inventoryRepository.save(InventoryItem.builder()
                .productId("PROD-002")
                .productName("Mouse")
                .availableQty(500)
                .reservedQty(0)
                .build());
            log.info("Inventory seeded with test products");
        }
    }
}
