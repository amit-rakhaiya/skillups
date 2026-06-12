package com.amit.skillup.order.inventoryservice.config;

import com.amit.skillup.order.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final InventoryService inventoryService;

    @Bean
    public ApplicationRunner seedInventory() {
        return args -> inventoryService.seedData();
    }
}
