package com.smartcharge.user.exception;

/**
 * Thrown when attempting to register with an e-mail address that already exists.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("A user with email '" + email + "' already exists");
    }
}
