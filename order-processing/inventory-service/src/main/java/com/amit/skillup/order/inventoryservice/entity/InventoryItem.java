package com.amit.skillup.order.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class InventoryItem {

    @Id
    private String productId;   // e.g., "PROD-001"

    private String productName;
    private int availableQty;
    private int reservedQty;
}
