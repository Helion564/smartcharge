package com.smartcharge.user.exception;

/**
 * Thrown when a provided refresh token is invalid, expired, or revoked.
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
