package com.smartcharge.booking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested booking resource cannot be found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(String message) {
        super(message);
    }

    public BookingNotFoundException(String bookingId, String userId) {
        super("Booking not found with id: " + bookingId + " for user: " + userId);
    }
}
