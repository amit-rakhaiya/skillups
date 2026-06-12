package com.amit.skillup.order.common.enums;

public enum OrderStatus {
    PENDING,
    VALIDATING,
    PAYMENT_PROCESSING,
    INVENTORY_CHECKING,
    PAYMENT_FAILED,
    INVENTORY_FAILED,
    SHIPPING,
    COMPLETED,
    FAILED,
    COMPENSATING
}
