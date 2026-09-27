package com.aaspaas.aaspaas_backend.delivery.enums;

/**
 * Single source of truth for delivery assignment lifecycle.
 */
public enum DeliveryAssignmentStatus {

    ASSIGNED,
    ACCEPTED,
    PICKUP_OTP_SENT,
    PICKED_UP,
    OUT_FOR_DELIVERY,
    DELIVERY_OTP_SENT,
    DELIVERED,
    REJECTED,
    CANCELLED,
    FAILED
}
