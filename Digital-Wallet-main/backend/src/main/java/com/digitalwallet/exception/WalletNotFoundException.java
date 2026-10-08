package com.digitalwallet.exception;

public class WalletNotFoundException extends ResourceNotFoundException {
    public WalletNotFoundException(Long id) { super("Wallet not found with id " + id); }
}
