package com.digitalwallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record TransferRequest(
        @NotNull(message = "senderWalletId is required") Long senderWalletId,
        @NotNull(message = "receiverWalletId is required") Long receiverWalletId,
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @Digits(integer = 13, fraction = 2, message = "Amount must have at most 2 decimal places") BigDecimal amount,
        @Size(max = 200, message = "Description must be at most 200 characters") String description) {
}
