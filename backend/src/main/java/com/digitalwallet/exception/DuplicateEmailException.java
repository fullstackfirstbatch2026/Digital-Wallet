package com.digitalwallet.exception;

public class DuplicateEmailException extends ConflictException {
    public DuplicateEmailException(String email) { super("Email already exists: " + email); }
}
