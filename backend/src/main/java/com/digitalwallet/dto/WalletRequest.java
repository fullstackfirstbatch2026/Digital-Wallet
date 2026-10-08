package com.digitalwallet.dto;

import jakarta.validation.constraints.NotNull;

public record WalletRequest(@NotNull(message = "userId is required") Long userId) {
}
