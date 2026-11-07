package com.smartcharge.booking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a booking operation is not permitted given the current state
 * (e.g., cancellation too close to start time, overlapping slots).
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class BookingConflictException extends RuntimeException {

    public BookingConflictException(String message) {
        super(message);
    }
}
