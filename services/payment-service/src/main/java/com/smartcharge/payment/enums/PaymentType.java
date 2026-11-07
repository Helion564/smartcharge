package com.smartcharge.payment.enums;

/**
 * Classifies the purpose or category of a payment transaction.
 */
public enum PaymentType {
    /** Charge for an EV charging session. */
    SESSION_CHARGE,
    /** Upfront charge for booking a charging slot. */
    BOOKING_CHARGE,
    /** Refund issued back to the user. */
    REFUND,
    /** User topping up their SmartCharge wallet. */
    WALLET_TOPUP
}
