package com.smartcharge.booking.enums;

/**
 * Represents the lifecycle states of an EV charging slot booking.
 */
public enum BookingStatus {

    /**
     * Initial state; payment or confirmation pending.
     */
    PENDING,

    /**
     * Booking has been confirmed and the slot is reserved.
     */
    CONFIRMED,

    /**
     * Booking was cancelled by the user or system before the session started.
     */
    CANCELLED,

    /**
     * The charging session associated with this booking has successfully completed.
     */
    COMPLETED,

    /**
     * User did not show up during the booked time window.
     */
    NO_SHOW,

    /**
     * Booking time window has passed without a session being initiated.
     */
    EXPIRED
}
