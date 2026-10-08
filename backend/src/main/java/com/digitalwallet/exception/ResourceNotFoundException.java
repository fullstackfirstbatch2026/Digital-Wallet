package com.digitalwallet.exception;

/** Base class for every "404 Not Found" error. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}
