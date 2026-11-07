package com.smartcharge.booking.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralized exception handler for the booking service REST layer.
 * Formats errors as RFC 7807 Problem Details.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BookingNotFoundException.class)
    public ProblemDetail handleBookingNotFound(BookingNotFoundException ex) {
        log.warn("Booking not found: {}", ex.getMessage());
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        detail.setType(URI.create("https://smartcharge.com/errors/booking-not-found"));
        detail.setTitle("Booking Not Found");
        detail.setProperty("timestamp", Instant.now().toString());
        return detail;
    }

    @ExceptionHandler(BookingConflictException.class)
    public ProblemDetail handleBookingConflict(BookingConflictException ex) {
        log.warn("Booking conflict: {}", ex.getMessage());
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        detail.setType(URI.create("https://smartcharge.com/errors/booking-conflict"));
        detail.setTitle("Booking Conflict");
        detail.setProperty("timestamp", Instant.now().toString());
        return detail;
    }

    @ExceptionHandler(InvalidBookingStateException.class)
    public ProblemDetail handleInvalidState(InvalidBookingStateException ex) {
        log.warn("Invalid booking state: {}", ex.getMessage());
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        detail.setType(URI.create("https://smartcharge.com/errors/invalid-booking-state"));
        detail.setTitle("Invalid Booking State");
        detail.setProperty("timestamp", Instant.now().toString());
        return detail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing
                ));
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "One or more fields failed validation");
        detail.setType(URI.create("https://smartcharge.com/errors/validation-error"));
        detail.setTitle("Validation Error");
        detail.setProperty("fieldErrors", fieldErrors);
        detail.setProperty("timestamp", Instant.now().toString());
        return detail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        detail.setType(URI.create("https://smartcharge.com/errors/bad-request"));
        detail.setTitle("Bad Request");
        detail.setProperty("timestamp", Instant.now().toString());
        return detail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        log.error("Unhandled exception in booking-service", ex);
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
        detail.setType(URI.create("https://smartcharge.com/errors/internal-error"));
        detail.setTitle("Internal Server Error");
        detail.setProperty("timestamp", Instant.now().toString());
        return detail;
    }
}
