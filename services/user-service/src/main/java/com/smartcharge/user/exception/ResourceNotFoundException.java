package com.smartcharge.user.exception;

import java.util.UUID;

/**
 * Thrown when a requested resource cannot be found in the data store.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, UUID id) {
        super(resourceName + " not found with id: " + id);
    }

    public ResourceNotFoundException(String resourceName, String field, String value) {
        super(resourceName + " not found with " + field + ": " + value);
    }
}
