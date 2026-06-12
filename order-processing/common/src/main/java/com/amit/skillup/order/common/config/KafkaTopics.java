package com.amit.skillup.order.common.config;

/**
 * Centralized Kafka topic constants — all services reference this class.
 * Avoids magic strings scattered across producers and consumers.
 */
public final class KafkaTopics {

    private KafkaTopics() {}

    public static final String ORDER_CREATED        = "order-created";
    public static final String PAYMENT_PROCESSED    = "payment-processed";
    public static final String INVENTORY_UPDATED    = "inventory-updated";
    public static final String ORDER_COMPLETED      = "order-completed";
    public static final String ORDER_FAILED         = "order-failed";
    public static final String SHIPPING_INITIATED   = "shipping-initiated";
    public static final String NOTIFICATION_EVENTS  = "notification-events";
}
