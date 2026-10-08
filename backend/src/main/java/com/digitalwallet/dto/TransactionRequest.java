package com.digitalwallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** transactionType must be DEPOSIT or WITHDRAW (transfers use /api/wallets/transfer). */
public record TransactionRequest(
        @NotNull(message = "walletId is required") Long walletId,
        @NotBlank(message = "transactionType is required") String transactionType,
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @Digits(integer = 13, fraction = 2, message = "Amount must have at most 2 decimal places") BigDecimal amount,
        @Size(max = 255, message = "Description must be at most 255 characters") String description) {
}
