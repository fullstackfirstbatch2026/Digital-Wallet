package com.digitalwallet.exception;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(Long id) { super("User not found with id " + id); }
}
