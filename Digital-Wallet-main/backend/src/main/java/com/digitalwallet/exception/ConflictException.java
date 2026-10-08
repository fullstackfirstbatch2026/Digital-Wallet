package com.digitalwallet.exception;

/** Base class for every "409 Conflict" error. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
