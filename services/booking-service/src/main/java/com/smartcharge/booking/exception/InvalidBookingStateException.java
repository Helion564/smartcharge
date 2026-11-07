package com.smartcharge.booking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a booking action is not allowed in the current booking state
 * (e.g., trying to cancel a COMPLETED booking).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidBookingStateException extends RuntimeException {

    public InvalidBookingStateException(String message) {
        super(message);
    }
}
