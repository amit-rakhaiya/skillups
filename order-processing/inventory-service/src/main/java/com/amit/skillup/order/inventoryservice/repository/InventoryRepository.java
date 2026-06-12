package com.amit.skillup.order.inventoryservice.repository;

import com.amit.skillup.order.inventoryservice.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<InventoryItem, String> {
}
