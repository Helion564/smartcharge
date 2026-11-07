package com.smartcharge.payment.enums;

/**
 * Represents the lifecycle states of a payment transaction.
 */
public enum PaymentStatus {
    /** Payment has been created but processing has not yet started. */
    PENDING,
    /** Payment is currently being processed by the gateway. */
    PROCESSING,
    /** Payment completed successfully. */
    SUCCESS,
    /** Payment processing failed. */
    FAILED,
    /** Full refund has been issued. */
    REFUNDED,
    /** Partial refund has been issued. */
    PARTIALLY_REFUNDED
}
